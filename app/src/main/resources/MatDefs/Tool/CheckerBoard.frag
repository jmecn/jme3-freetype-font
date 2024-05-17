#import "Common/ShaderLib/GLSLCompat.glsllib"

#ifdef HAS_COLOR
uniform vec4 m_Color;
#endif

varying vec2 uv;

// --- analytically box-filtered checkerboard ---
// https://www.shadertoy.com/view/XlcSz2
float checkersTextureGradBox( vec2 p, vec2 ddx, vec2 ddy ) {
    // filter kernel
    vec2 w = max(abs(ddx), abs(ddy)) + 0.01;
    // analytical integral (box filter)
    vec2 i = 2.0*(abs(fract((p-0.5*w)/2.0)-0.5)-abs(fract((p+0.5*w)/2.0)-0.5))/w;
    // xor pattern
    return 0.5-0.5*i.x*i.y;
}

void main(){
    vec4 color = vec4(1.0);

    #ifdef HAS_COLOR
    color *= m_Color;
    #endif

    // calc texture sampling footprint
    vec2 ddx_uv = dFdx(uv);
    vec2 ddy_uv = dFdy(uv);

    // shading
    vec3 mate = vec3(1.0) * checkersTextureGradBox(uv, ddx_uv, ddy_uv);

    gl_FragColor = color * vec4(mate, 1.0);
}