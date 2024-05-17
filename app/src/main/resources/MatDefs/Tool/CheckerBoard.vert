#import "Common/ShaderLib/GLSLCompat.glsllib"

uniform mat4 g_WorldViewProjectionMatrix;
uniform mat4 g_WorldMatrix;

attribute vec3 inPosition;

varying vec2 uv;

void main(){
    vec4 modelSpacePos = vec4(inPosition, 1.0);

    // use model xz as uv
    uv = (g_WorldMatrix * modelSpacePos).xz;

    gl_Position = g_WorldViewProjectionMatrix * modelSpacePos;
}