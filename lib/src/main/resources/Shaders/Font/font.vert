#import "Common/ShaderLib/GLSLCompat.glsllib"
#import "Common/ShaderLib/Instancing.glsllib"

attribute vec3 inPosition;
attribute vec2 inTexCoord;
attribute vec4 inVertColor;

varying vec2 vTexCoord;
varying vec4 vVertColor;

void main() {
    vTexCoord = inTexCoord;
    vVertColor = inVertColor;

    vec3 position = inPosition;
    vec4 modelSpacePos = vec4(position, 1.0);

    gl_Position = TransformWorldViewProjection(modelSpacePos);
}