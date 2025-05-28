import path, { dirname, resolve } from 'path';
import fs, { promises as fsPromises } from 'fs';
import { PNG } from 'pngjs';
import pixelmatch from 'pixelmatch';
import { fileURLToPath } from 'url';
import { globSync } from 'glob';
import puppeteer, { type Page, type Browser } from 'puppeteer';
import type { PointLike, StyleSpecification } from 'maplibre-gl';
import junitReportBuilder, { type TestSuite } from 'junit-report-builder';
import type * as maplibreglModule from 'maplibre-gl';
import * as pmtiles from 'pmtiles';
import express from 'express';
import cors from 'cors';
import { layers, namedFlavor } from "@protomaps/basemaps";

const __dirname = resolve(dirname(fileURLToPath(import.meta.url)), '..');
let maplibregl: typeof maplibreglModule;

type TestData = {
    id: string;
    width: number;
    height: number;
    pixelRatio: number;
    flavor: string;
    lang: string;
    recycleMap: boolean;
    allowed: number;
    /**
     * Perceptual color difference threshold, number between 0 and 1, smaller is more sensitive
     * @defaultValue 0.1285
     */
    threshold: number;
    ok: boolean;
    difference: number;
    timeout: number;
    addFakeCanvas: {
        id: string;
        image: string;
    };
    fadeDuration: number;
    debug: boolean;
    showOverdrawInspector: boolean;
    showPadding: boolean;
    collisionDebug: boolean;
    localIdeographFontFamily: string;
    crossSourceCollisions: boolean;
    queryGeometry: PointLike;
    queryOptions: any;
    error?: Error;
    maxPitch: number;
    continuesRepaint: boolean;
    // Crop PNG results if they're too large
    reportWidth: number;
    reportHeight: number;

    // base64-encoded content of the PNG results
    actual: string;
    diff: string;
    expected: string;
};

type RenderOptions = {
    tests: any[];
    recycleMap: boolean;
    skipreport: boolean;
    seed: string;
    debug: boolean;
    openBrowser: boolean;
};

type StyleWithTestData = StyleSpecification & {
    metadata: {
        test: TestData;
    };
};

type TestStats = {
    total: number;
    errored: TestData[];
    failed: TestData[];
    passed: TestData[];
};

// https://stackoverflow.com/a/1349426/229714
function makeHash(): string {
    const array = [];
    const possible = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';

    for (let i = 0; i < 10; ++i)
        array.push(possible.charAt(Math.floor(Math.random() * possible.length)));

    // join array elements without commas.
    return array.join('');
}

function checkParameter(options: RenderOptions, param: string): boolean {
    const index = options.tests.indexOf(param);
    if (index === -1)
        return false;
    options.tests.splice(index, 1);
    return true;
}

function checkValueParameter(options: RenderOptions, defaultValue: any, param: string) {
    const index = options.tests.findIndex((elem) => { return String(elem).startsWith(param); });
    if (index === -1)
        return defaultValue;

    const split = String(options.tests.splice(index, 1)).split('=');
    if (split.length !== 2)
        return defaultValue;

    return split[1];
}
/**
 * Compares the Unit8Array that was created to the expected file in the file system.
 * It updates testData with the results.
 *
 * @param directory - The base directory of the data
 * @param testData - The test data
 * @param data - The actual image data to compare the expected to
 * @returns nothing as it updates the testData object
 */
function compareRenderResults(directory: string, testData: TestData, data: Uint8Array) {
    const dir = path.join(directory, testData.id);
    if (!fs.existsSync(dir)) {
        fs.mkdirSync(dir);
    }

    const expectedPath = path.join(dir, 'expected.png');
    const actualPath = path.join(dir, 'actual.png');
    const diffPath = path.join(dir, 'diff.png');

    const width = Math.floor(testData.reportWidth ?? testData.width * testData.pixelRatio);
    const height = Math.floor(testData.reportHeight ?? testData.height * testData.pixelRatio);
    const actualImg = new PNG({ width, height });

    // PNG data must be unassociated (not premultiplied)
    for (let i = 0; i < data.length; i++) {
        const a = data[i * 4 + 3] / 255;
        if (a !== 0) {
            data[i * 4 + 0] /= a;
            data[i * 4 + 1] /= a;
            data[i * 4 + 2] /= a;
        }
    }
    actualImg.data = data as any;
    const actualBuf = PNG.sync.write(actualImg, { filterType: 4 });
    testData.actual = actualBuf.toString('base64');

    // there may be multiple expected images, covering different platforms
    let globPattern = path.join(dir, 'expected*.png');
    globPattern = globPattern.replace(/\\/g, '/');
    const expectedPaths = globSync(globPattern);

    if (!process.env.UPDATE && expectedPaths.length === 0) {
        throw new Error(`No expected*.png files found as ${dir}; did you mean to run tests with UPDATE=true?`);
    }

    // if we have multiple expected images, we'll compare against each one and pick the one with
    // the least amount of difference; this is useful for covering features that render differently
    // depending on platform, i.e. heatmaps use half-float textures for improved rendering where supported
    let minDiff = Infinity;
    let minDiffImg: PNG;
    let minExpectedBuf: Buffer;

    for (const path of expectedPaths) {
        const expectedBuf = fs.readFileSync(path);
        const expectedImg = PNG.sync.read(expectedBuf);
        const diffImg = new PNG({ width, height });
        if (!testData.expected) {
            testData.expected = expectedBuf.toString('base64'); // default expected image
        }

        const diff = pixelmatch(
            actualImg.data, expectedImg.data, diffImg.data,
            width, height, { threshold: testData.threshold }) / (width * height);

        if (diff < minDiff) {
            minDiff = diff;
            minDiffImg = diffImg;
            minExpectedBuf = expectedBuf;
        }
    }

    if (minDiffImg) {
        const diffBuf = PNG.sync.write(minDiffImg, { filterType: 4 });
        fs.writeFileSync(diffPath, diffBuf);
        testData.diff = diffBuf.toString('base64');
        testData.expected = minExpectedBuf.toString('base64');
    }
    fs.writeFileSync(actualPath, actualBuf);

    testData.difference = minDiff;
    testData.ok = minDiff <= testData.allowed;

    if (!testData.ok && process.env.UPDATE) {
        console.log(`Updating ${expectedPath}`);
        fs.writeFileSync(expectedPath, PNG.sync.write(actualImg));
    }
}

/**
 * Gets all the tests from the file system looking for style.json files.
 *
 * @param options - The options
 * @param directory - The base directory
 * @returns The tests data structure and the styles that were loaded
 */
function getTestStyles(options: RenderOptions, directory: string): StyleWithTestData[] {
    const tests = options.tests || [];

    const sequence = globSync('**/style.json', { cwd: directory })
        .map(fixture => {
            const id = path.dirname(fixture);
            const style = JSON.parse(fs.readFileSync(path.join(directory, fixture), 'utf8')) as StyleWithTestData;
            style.metadata = style.metadata || {} as any;

            style.metadata.test = {
                id,
                width: 512,
                height: 512,
                pixelRatio: 1,
                flavor: 'light',
                lang: 'en',
                recycleMap: options.recycleMap || false,
                allowed: 0.00025,
                threshold: 0.05,
                ...style.metadata.test
            };

            style.layers = layers("protomaps", namedFlavor(style.metadata.test.flavor), { lang: style.metadata.test.lang });
            style.sprite = `https://protomaps.github.io/basemaps-assets/sprites/v4/${style.metadata.test.flavor}`;
            style.glyphs = "https://protomaps.github.io/basemaps-assets/fonts/{fontstack}/{range}.pbf";
            style.sources = {
                "protomaps": {
                    "type": "vector",
                    "url": "pmtiles://http://localhost:2900/tiles.pmtiles"
                }
            };
            return style;
        })
        .filter(style => {
            const test = style.metadata.test;
            if (tests.length !== 0 && !tests.some(t => test.id.indexOf(t) !== -1)) {
                return false;
            }

            if (process.env.BUILDTYPE !== 'Debug' && test.id.match(/^debug\//)) {
                console.log(`* skipped ${test.id}`);
                return false;
            }
            return true;
        });
    return sequence;
}

/**
 * It creates the map and applies the operations to create an image
 * and returns it as a Uint8Array
 *
 * @param style - The style to use
 * @returns an image byte array promise
 */
async function getImageFromStyle(styleForTest: StyleWithTestData, page: Page): Promise<Uint8Array> {

    const width = styleForTest.metadata.test.width;
    const height = styleForTest.metadata.test.height;

    await page.setViewport({ width, height, deviceScaleFactor: 2 });

    await page.setContent(`
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Query Test Page</title>
    <meta charset='utf-8'>
    <link rel="icon" href="about:blank">
    <style>#map {
        box-sizing:content-box;
        width:${width}px;
        height:${height}px;
    }</style>
</head>
<body>
    <div id='map'></div>
</body>
</html>`);

    const evaluatedArray = await page.evaluate(async (style: StyleWithTestData) => {

        const options = style.metadata.test;

        return new Promise(async (resolve, reject) => {
            setTimeout(() => {
                reject(new Error('Test timed out'));
            }, options.timeout || 4000);


            if (maplibregl.getRTLTextPluginStatus() === 'unavailable') {
                maplibregl.setRTLTextPlugin(
                    'https://unpkg.com/@mapbox/mapbox-gl-rtl-text@0.3.0/dist/mapbox-gl-rtl-text.js',
                    false // Don't lazy load the plugin
                );
            }

            const protocol = new pmtiles.Protocol();
            maplibregl.addProtocol('pmtiles', protocol.tile);

            const map = new maplibregl.Map({
                container: 'map',
                style,
                interactive: false,
                attributionControl: false,
                maxPitch: options.maxPitch,
                pixelRatio: options.pixelRatio,
                canvasContextAttributes: { preserveDrawingBuffer: true, powerPreference: 'default' },
                fadeDuration: options.fadeDuration || 0,
                localIdeographFontFamily: options.localIdeographFontFamily || false as any,
                crossSourceCollisions: typeof options.crossSourceCollisions === 'undefined' ? true : options.crossSourceCollisions,
                maxCanvasSize: [8192, 8192]
            });

            let idle = false;
            map.on('idle', () => { console.log('idle'); idle = true; });
            // Configure the map to never stop the render loop
            map.repaint = typeof options.continuesRepaint === 'undefined' ? true : options.continuesRepaint;

            if (options.debug) map.showTileBoundaries = true;
            if (options.showOverdrawInspector) map.showOverdrawInspector = true;
            if (options.showPadding) map.showPadding = true;

            const gl = map.painter.context.gl;

            await map.once('load');
            if (options.collisionDebug) {
                map.showCollisionBoxes = true;
            }

            const viewport = gl.getParameter(gl.VIEWPORT);
            const w = options.reportWidth ?? viewport[2];
            const h = options.reportHeight ?? viewport[3];

            const data = new Uint8Array(w * h * 4);
            gl.readPixels(0, 0, w, h, gl.RGBA, gl.UNSIGNED_BYTE, data);

            // Flip the scanlines.
            const stride = w * 4;
            const tmp = new Uint8Array(stride);
            for (let i = 0, j = h - 1; i < j; i++, j--) {
                const start = i * stride;
                const end = j * stride;
                tmp.set(data.slice(start, start + stride), 0);
                data.set(data.slice(end, end + stride), start);
                data.set(tmp, end);
            }

            map.remove();
            delete map.painter.context.gl;

            if (options.addFakeCanvas) {
                const fakeCanvas = window.document.getElementById(options.addFakeCanvas.id);
                fakeCanvas.parentNode.removeChild(fakeCanvas);
            }

            resolve(data);
        });
    }, styleForTest as any);

    return new Uint8Array(Object.values(evaluatedArray as object) as number[]);
}

/**
 * Prints the progress to the console
 *
 * @param test - The current test
 * @param total - The total number of tests
 * @param index - The current test index
 */
function printProgress(test: TestData, total: number, index: number) {
    if (test.error) {
        console.log('\x1b[91m', `${index}/${total}: errored ${test.id} ${test.error.message}`, '\x1b[0m');
    } else if (!test.ok) {
        console.log('\x1b[31m', `${index}/${total}: failed ${test.id} ${test.difference}`, '\x1b[0m');
    } else {
        console.log(`${index}/${total}: passed ${test.id}`);
    }
}

function printSpecificStatistics(status: 'passed' | 'failed' | 'errored', subsetStats: TestData[], total: number, suite: TestSuite) {
    const statusCount = subsetStats.length;
    if (statusCount === 0) {
        return;
    }
    console.log(`${statusCount} ${status} (${(100 * statusCount / total).toFixed(1)}%)`);
    for (const testData of subsetStats) {
        const testCase = suite.testCase().className(testData.id).name(testData.id);
        if (status === 'failed') {
            testCase.failure();
        } else if (status === 'errored') {
            testCase.error();
        }
    }
    if (status === 'passed') {
        return;
    }
    for (let i = 0; i < subsetStats.length; i++) {
        printProgress(subsetStats[i], statusCount, i + 1);
    }
}

/**
 * Prints the summary at the end of the run
 *
 * @param tests - all the tests with their results
 * @returns `true` if all the tests passed
 */
function printStatistics(stats: TestStats): boolean {
    const suite = junitReportBuilder.testSuite().name('render-tests');
    printSpecificStatistics('passed', stats.passed, stats.total, suite);
    printSpecificStatistics('failed', stats.failed, stats.total, suite);
    printSpecificStatistics('errored', stats.errored, stats.total, suite);

    junitReportBuilder.writeTo('junit.xml');
    return (stats.failed.length + stats.errored.length) === 0;
}

function getReportItem(test: TestData) {
    return `<div class="test">
    <h2>${test.id}</h2>
    ${test.actual ? `
    <div class="imagewrap">
        <div>
        <p>Actual</p>
        <img src="data:image/png;base64,${test.actual}" data-alt-src="data:image/png;base64,${test.expected}">
        </div>
        ${test.diff ? `
        <div>
        <p>Diff</p>
        <img src="data:image/png;base64,${test.diff}" data-alt-src="data:image/png;base64,${test.expected}">
        </div>` : ''}
        ${test.expected ? `
        <div>
        <p>Closest expected</p>
        <img src="data:image/png;base64,${test.expected}"  >
        </div>` : ''}
    </div>` : ''}
    ${test.error ? `<p style="color: red"><strong>Error:</strong> ${test.error.message}</p>` : ''}
    ${test.difference ? `<p class="diff"><strong>Diff:</strong> ${test.difference}</p>` : ''}
</div>`;
}

function applyDebugParameter(options: RenderOptions, page: Page) {
    if (options.debug) {
        page.on('console', async (message) => {
            if (message.text() !== 'JSHandle@error') {
                console.log(`${message.type().substring(0, 3).toUpperCase()} ${message.text()}`);
                return;
            }
            const messages = await Promise.all(message.args().map((arg) => arg.getProperty('message')));
            console.log(`${message.type().substring(0, 3).toUpperCase()} ${messages.filter(Boolean)}`);
        });

        page.on('pageerror', ({ message }) => console.error(message));

        page.on('response', response =>
            console.log(`${response.status()} ${response.url()}`));

        page.on('requestfailed', request => {
            if (request) {
                console.error(`requestfailed, error text: ${request.failure()?.errorText}, url: ${request.url()}`);
            } else {
                console.error('Request failed and request object is ', request);
            }
        });
    }
}

async function runTests(page: Page, testStyles: StyleWithTestData[], directory: string) {
    let index = 0;
    for (const style of testStyles) {
        try {
            style.metadata.test.error = undefined;
            const data = await getImageFromStyle(style, page);
            compareRenderResults(directory, style.metadata.test, data);
        } catch (ex) {
            style.metadata.test.error = ex;
        }
        printProgress(style.metadata.test, testStyles.length, ++index);
    }
}

async function createPageAndStart(browser: Browser, testStyles: StyleWithTestData[], directory: string, options: RenderOptions) {
    const page = await browser.newPage();
    applyDebugParameter(options, page);
    await page.addScriptTag({ path: 'node_modules/maplibre-gl/dist/maplibre-gl-dev.js' });
    await page.addScriptTag({ path: 'node_modules/pmtiles/dist/pmtiles.js' });
    await runTests(page, testStyles, directory);
    return page;
}

/**
 * Entry point to run the render test suite, compute differences to expected values (making exceptions based on
 * implementation vagaries), print results to standard output, write test artifacts to the
 * filesystem (optionally updating expected results), and exit the process with a success or
 * failure code.
 *
 * If all the tests are successful, this function exits the process with exit code 0. Otherwise
 * it exits with 1.
 */
async function executeRenderTests() {
    const options: RenderOptions = {
        tests: [],
        recycleMap: false,
        skipreport: false,
        seed: makeHash(),
        debug: false,
        openBrowser: false
    };

    if (process.argv.length > 2) {
        options.tests = process.argv.slice(2).filter((value, index, self) => { return self.indexOf(value) === index; }) || [];
        options.recycleMap = checkParameter(options, '--recycle-map');
        options.skipreport = checkParameter(options, '--skip-report');
        options.seed = checkValueParameter(options, options.seed, '--seed');
        options.debug = checkParameter(options, '--debug');
        options.openBrowser = checkParameter(options, '--open-browser');
    }

    const browser = await puppeteer.launch({
        headless: !options.openBrowser,
        args: [
            '--enable-webgl',
            '--no-sandbox',
            '--disable-web-security'
        ]
    });

    const pmtilesFilePath = 'pmtiles/tiles.pmtiles';
    try {
        await fsPromises.access(pmtilesFilePath);
    }
    catch {
        console.error(`The PMTiles file "${pmtilesFilePath}" does not exist. Try running "./generate_pmtiles.sh" first.`);
        process.exit(1);
    }
    const pmtilesServerApp = express();
    pmtilesServerApp.use(cors());
    pmtilesServerApp.use(express.static('pmtiles'));
    await new Promise<void>((resolve) => pmtilesServerApp.listen(2900, '0.0.0.0', resolve));

    const directory = path.join(__dirname);
    let testStyles = getTestStyles(options, directory);

    if (process.env.SPLIT_COUNT && process.env.CURRENT_SPLIT_INDEX) {
        const numberOfTestsForThisPart = Math.ceil(testStyles.length / +process.env.SPLIT_COUNT);
        testStyles = testStyles.splice(+process.env.CURRENT_SPLIT_INDEX * numberOfTestsForThisPart, numberOfTestsForThisPart);
    }

    let page = await createPageAndStart(browser, testStyles, directory, options);
    await page.close();

    const tests = testStyles.map(s => s.metadata.test).filter(t => !!t);
    const testStats: TestStats = {
        total: tests.length,
        errored: tests.filter(t => t.error),
        failed: tests.filter(t => !t.error && !t.ok),
        passed: tests.filter(t => !t.error && t.ok)
    };

    if (process.env.UPDATE) {
        if (testStats.errored.length > 0) {
            console.log(`Updated ${testStats.failed.length}/${testStats.total} tests, ${testStats.errored.length} errored.`);
        } else {
            console.log(`Updated ${testStats.total} tests.`);
        }
        process.exit(0);
    }

    const success = printStatistics(testStats);

    if (!options.skipreport) {
        const erroredItems = testStats.errored.map(t => getReportItem(t));
        const failedItems = testStats.failed.map(t => getReportItem(t));

        // write HTML reports
        let resultData: string;
        if (erroredItems.length || failedItems.length) {
            const resultItemTemplate = fs.readFileSync(path.join(__dirname, 'result_item_template.html')).toString();
            resultData = resultItemTemplate
                .replace('${failedItemsLength}', failedItems.length.toString())
                .replace('${failedItems}', failedItems.join('\n'))
                .replace('${erroredItemsLength}', erroredItems.length.toString())
                .replace('${erroredItems}', erroredItems.join('\n'));
        } else {
            resultData = '<h1 style="color: green">All tests passed!</h1>';
        }

        const reportTemplate = fs.readFileSync(path.join(__dirname, 'report_template.html')).toString();
        const resultsContent = reportTemplate.replace('${resultData}', resultData);

        const p = path.join(__dirname, options.recycleMap ? 'results-recycle-map.html' : 'results.html');
        fs.writeFileSync(p, resultsContent, 'utf8');
        console.log(`\nFull html report is logged to '${p}'`);

        // write text report of just the error/failed id
        if (testStats.errored?.length > 0) {
            const erroredItemIds = testStats.errored.map(t => t.id);
            const caseIdFileName = path.join(__dirname, 'results-errored-caseIds.txt');
            fs.writeFileSync(caseIdFileName, erroredItemIds.join('\n'), 'utf8');

            console.log(`\n${testStats.errored?.length} errored test case IDs are logged to '${caseIdFileName}'`);
        }

        if (testStats.failed?.length > 0) {
            const failedItemIds = testStats.failed.map(t => t.id);
            const caseIdFileName = path.join(__dirname, 'results-failed-caseIds.txt');
            fs.writeFileSync(caseIdFileName, failedItemIds.join('\n'), 'utf8');

            console.log(`\n${testStats.failed?.length} failed test case IDs are logged to '${caseIdFileName}'`);
        }
    }

    process.exit(success ? 0 : 1);
}

// start testing here
executeRenderTests();
