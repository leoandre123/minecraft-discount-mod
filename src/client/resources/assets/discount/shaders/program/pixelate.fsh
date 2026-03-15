#version 150

uniform sampler2D DiffuseSampler;
uniform ivec2 PixelSize;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    // Use the actual render target size from the texture itself
    vec2 texSize = vec2(textureSize(DiffuseSampler, 0));
    vec2 stepUV  = vec2(PixelSize) / texSize;
    vec2 snapped = floor(texCoord / stepUV) * stepUV;
    vec2 center  = snapped + 0.5 * stepUV;
    fragColor = texture(DiffuseSampler, center);
}
