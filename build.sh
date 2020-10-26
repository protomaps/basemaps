mkdir -p build/mbgl
mbgl/generate_mbgl_style mbgl/base.json > build/mbgl/light.json
mbgl/generate_mbgl_style mbgl/base.json mbgl/black > build/mbgl/black.json
mbgl/generate_mbgl_style mbgl/base.json mbgl/dark > build/mbgl/dark.json
mbgl/generate_mbgl_style mbgl/base.json mbgl/grayscale > build/mbgl/grayscale.json
mbgl/generate_mbgl_style mbgl/base.json mbgl/white > build/mbgl/white.json
mkdir -p build/tangram
tangram/generate_tangram_style tangram/base.yml > build/tangram/light.yml
tangram/generate_tangram_style tangram/base.yml tangram/black > build/tangram/black.yml
tangram/generate_tangram_style tangram/base.yml tangram/dark > build/tangram/dark.yml
tangram/generate_tangram_style tangram/base.yml tangram/grayscale > build/tangram/grayscale.yml
tangram/generate_tangram_style tangram/base.yml tangram/white > build/tangram/white.yml