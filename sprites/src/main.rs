extern crate xmltree;

use rayon::prelude::*;
use spreet::resvg::tiny_skia::Pixmap;
use spreet::sprite;
use std::collections::BTreeMap;
use std::fs;
use std::fs::File;
use std::io::Cursor;
use xmltree::{Element, XMLNode};

use spreet::resvg::usvg::TreeParsing;
use spreet::resvg::usvg::{Node, NodeExt, NodeKind, Options, Rect, Tree};

use serde::{Deserialize, Serialize};
use std::collections::HashMap;

use clap::Parser;

use regex::Regex;

fn parse_color(color: &str) -> Option<(u8, u8, u8)> {
    match color.len() {
        3 => Some((
            u8::from_str_radix(&color[0..1].repeat(2), 16).ok()?,
            u8::from_str_radix(&color[1..2].repeat(2), 16).ok()?,
            u8::from_str_radix(&color[2..3].repeat(2), 16).ok()?,
        )),
        6 => Some((
            u8::from_str_radix(&color[0..2], 16).ok()?,
            u8::from_str_radix(&color[2..4], 16).ok()?,
            u8::from_str_radix(&color[4..6], 16).ok()?,
        )),
        _ => None,
    }
}

fn mix(a: u8, b: u8, t: f32) -> u8 {
    ((1.0 - t) * a as f32 + t * b as f32)
        .round()
        .clamp(0.0, 255.0) as u8
}

fn process_colors<F>(input: &str, modifier: &F) -> String
where
    F: Fn(u8, u8, u8) -> String,
{
    let re = Regex::new(r"#([0-9a-fA-F]{3}(?:[0-9a-fA-F]{3})?)").unwrap();
    let mut last_end = 0;
    let mut result = String::new();

    // Iterate over each color code found in the string.
    for cap in re.captures_iter(input) {
        let color_code = &cap[1];
        let (r, g, b) = parse_color(color_code).unwrap();

        let modified_color = modifier(r, g, b);

        // Append substring from the end of the last match to the start of this match.
        result.push_str(&input[last_end..cap.get(0).unwrap().start()]);

        // Append the modified color code.
        result.push_str(&modified_color);

        // Update the end of the last match.
        last_end = cap.get(0).unwrap().end();
    }

    // Append the remainder of the input string after the last match.
    result.push_str(&input[last_end..]);

    result
}

fn compute_bbox(node: &Node, bboxes: &mut Vec<Rect>) {
    match *node.borrow() {
        NodeKind::Path(ref _path) => {
            let bbox = node.calculate_bbox().unwrap();
            bboxes.push(bbox);
        }
        NodeKind::Group(_) => {
            for child in node.children() {
                compute_bbox(&child, bboxes);
            }
        }
        _ => {}
    }
}

fn remove_text_elements(elem: &mut Element) {
    elem.children.retain(|node| {
        if let XMLNode::Text(_) = node {
            false
        } else {
            true
        }
    });

    for child in &mut elem.children {
        if let XMLNode::Element(child_elem) = child {
            remove_text_elements(child_elem);
        }
    }
}

fn extract_groups_from_svg(
    input_svg_path: &str,
    shader: &Shader,
    svg_datas: &mut Vec<(String, Vec<u8>)>,
) {
    let mut tree = Element::parse(File::open(input_svg_path).unwrap()).unwrap();

    let cloned = tree.clone();
    let defs_element = cloned.children.iter().find(|node| {
        if let xmltree::XMLNode::Element(el) = node {
            el.name == "defs"
        } else {
            false
        }
    });

    let options = Options::default();

    let ignore = vec![
        "priority",
        "guides",
        "categories",
        "in_tiles_now",
        "add_to_tiles",
    ];

    for child_node in &mut tree.children {
        if let XMLNode::Element(child_elem) = child_node {
            if child_elem.name == "g" {
                if let Some(name) = child_elem.attributes.get("id") {
                    // ignore certain icons in the Illustrator doc.
                    if ignore.contains(&name.as_str()) {
                        continue;
                    }

                    // determine the icon name.
                    let mut substr = name[1..].to_string();

                    // hardcoded renames
                    if substr == "townspot-s-rev" {
                        substr = "townspot".to_string();
                    }

                    if substr == "capital-s" {
                        substr = "capital".to_string();
                    }

                    if !shader.icons.contains_key(&substr) {
                        continue;
                    }

                    // ignore helper labels.
                    remove_text_elements(child_elem);

                    // create a temporary SVG doc for calculating the viewBox

                    let mut tmp_root_attributes = std::collections::HashMap::new();
                    tmp_root_attributes.insert(
                        String::from("xmlns"),
                        String::from("http://www.w3.org/2000/svg"),
                    );

                    let mut tmp_root = Element {
                        name: String::from("svg"),
                        attributes: tmp_root_attributes,
                        children: Vec::new(),
                        namespace: None,
                        namespaces: None,
                        prefix: None,
                    };

                    tmp_root
                        .children
                        .push(xmltree::XMLNode::Element(child_elem.clone()));

                    let mut tmp_buffer = Cursor::new(Vec::new());
                    tmp_root.write(&mut tmp_buffer).unwrap();
                    let tmp_svg = Tree::from_data(&tmp_buffer.into_inner(), &options);

                    let mut bboxes = Vec::new();
                    compute_bbox(&tmp_svg.unwrap().root, &mut bboxes);

                    let mut min_x = f32::MAX;
                    let mut min_y = f32::MAX;
                    let mut max_x = f32::MIN;
                    let mut max_y = f32::MIN;

                    for rect in &bboxes {
                        min_x = min_x.min(rect.x());
                        max_x = max_x.max(rect.x() + rect.width());
                        min_y = min_y.min(rect.y());
                        max_y = max_y.max(rect.y() + rect.height());
                    }

                    min_x -= 0.5;
                    max_x += 0.5;
                    min_y -= 0.5;
                    max_y += 0.5;

                    let view_box =
                        format!("{} {} {} {}", min_x, min_y, max_x - min_x, max_y - min_y);

                    // create the final svg data
                    let mut root_attributes = std::collections::HashMap::new();
                    root_attributes.insert(String::from("viewBox"), String::from(view_box));
                    root_attributes.insert(
                        String::from("xmlns"),
                        String::from("http://www.w3.org/2000/svg"),
                    );

                    let mut root = Element {
                        name: String::from("svg"),
                        attributes: root_attributes,
                        children: Vec::new(),
                        namespace: None,
                        namespaces: None,
                        prefix: None,
                    };

                    let mut cloned_defs = defs_element.unwrap().clone();

                    if shader.icons.contains_key(&substr) {
                        let black = parse_color(
                            shader.flavors[&shader.icons[&substr]][0]
                                .strip_prefix("#")
                                .unwrap(),
                        )
                        .unwrap();
                        let white = parse_color(
                            shader.flavors[&shader.icons[&substr]][1]
                                .strip_prefix("#")
                                .unwrap(),
                        )
                        .unwrap();

                        println!("{:?}, {:?}, {:?}", substr, black, white);

                        // there is a theme, let's modify

                        if let Some(style) =
                            cloned_defs.as_mut_element().unwrap().get_mut_child("style")
                        {
                            // Modify the text of the style tag
                            if let Some(child) = style
                                .children
                                .iter_mut()
                                .find(|node| matches!(node, XMLNode::Text(_)))
                            {
                                if let XMLNode::Text(text) = child {
                                    let output = process_colors(text, &|r, g, b| {
                                        let new_r = mix(black.0, white.0, (r as f32) / 255.0);
                                        let new_g = mix(black.1, white.1, (g as f32) / 255.0);
                                        let new_b = mix(black.2, white.2, (b as f32) / 255.0);

                                        let modified_color =
                                            format!("#{:02x}{:02x}{:02x}", new_r, new_g, new_b);
                                        modified_color
                                    });
                                    *child = XMLNode::Text(output);
                                }
                            }
                        }
                    }

                    root.children.push(cloned_defs);

                    root.children
                        .push(xmltree::XMLNode::Element(child_elem.clone()));

                    let mut buffer = Cursor::new(Vec::new());
                    root.write(&mut buffer).unwrap();

                    svg_datas.push((substr, buffer.into_inner()));
                }
            }
        }
    }
}

#[derive(Debug, Serialize, Deserialize)]
struct Shader {
    flavors: HashMap<String, Vec<String>>,
    icons: HashMap<String, String>,
}

fn load_shader<P: AsRef<std::path::Path>>(path: P) -> Result<Shader, Box<dyn std::error::Error>> {
    let file_content = fs::read_to_string(path)?;
    let shader: Shader = serde_json::from_str(&file_content)?;
    Ok(shader)
}

#[derive(Parser)]
pub struct Cli {
    pub input: String,
    pub shader: String,
    /// Name of the file in which to save the spritesheet
    pub output: String,
}

fn main() {
    let args = Cli::parse();

    let shader = load_shader(&args.shader).unwrap();

    let mut svg_datas = Vec::new();
    extract_groups_from_svg(&args.input, &shader, &mut svg_datas);

    for ratio in [1,2] {
        let options = Options::default();
        let sprites = svg_datas
            .par_iter()
            .map(|p| {
                let svg = spreet::resvg::usvg::Tree::from_data(&p.1, &options).unwrap();
                (
                    p.0.clone(),
                    sprite::generate_pixmap_from_svg(&svg, ratio).unwrap(),
                )
            })
            .collect::<BTreeMap<String, Pixmap>>();

        if sprites.is_empty() {
            eprintln!("Error: no valid SVGs found in {:?}", &args.input);
            std::process::exit(exitcode::NOINPUT);
        }

        let mut spritesheet_builder = sprite::Spritesheet::build();
        spritesheet_builder.sprites(sprites).pixel_ratio(ratio);
        spritesheet_builder.make_unique();

        // Generate sprite sheet
        let Some(spritesheet) = spritesheet_builder.generate() else {
            eprintln!("Error: could not pack the sprites within an area fifty times their size.");
            std::process::exit(exitcode::DATAERR);
        };

        // Save the bitmapped spritesheet to a local PNG.
        let file_prefix = format!("{}{}", &args.output, if ratio == 2 { "@2x" } else { "" });
        let spritesheet_path = format!("{file_prefix}.png");
        if let Err(e) = spritesheet.save_spritesheet(&spritesheet_path) {
            eprintln!("Error: could not save spritesheet to {spritesheet_path} ({e})");
            std::process::exit(exitcode::IOERR);
        };

        // Save the index file to a local JSON file with the same name as the spritesheet.
        if let Err(e) = spritesheet.save_index(&file_prefix, true) {
            eprintln!("Error: could not save sprite index to {file_prefix} ({e})");
            std::process::exit(exitcode::IOERR);
        };
    }
}
