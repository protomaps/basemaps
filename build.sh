mkdir -p build/mbgl
mbgl/generate_mbgl_style mbgl/base.json > build/mbgl/light.json
mbgl/generate_mbgl_style mbgl/base.json mbgl/black > build/mbgl/black.json
mbgl/generate_mbgl_style mbgl/base.json mbgl/dark > build/mbgl/dark.json
mbgl/generate_mbgl_style mbgl/base.json mbgl/grayscale > build/mbgl/grayscale.json
mbgl/generate_mbgl_style mbgl/base.json mbgl/white > build/mbgl/white.json
