#import "Common/ShaderLib/GLSLCompat.glsllib"

uniform vec4 m_Color;
uniform sampler2D m_ColorMap;

#if defined(DISCARD_ALPHA)
uniform float m_AlphaDiscardThreshold;
#endif

const float glyph_center   = 0.50;
vec3 outline_color  = vec3(1.0,0.0,0.0);
const float outline_center = 0.55;
vec3 glow_color     = vec3(1.0,1.0,1.0);
const float glow_center    = 1.25;

varying vec2 vTexCoord;

varying vec4 vVertColor;

void main(void) {
    vec2 uv = vTexCoord.xy;
    vec4 color = vec4(1.0);

    #ifdef SDF_USE_ALPHA
    float dist = texture2D(m_ColorMap, uv).a;
    #else
    float dist = texture2D(m_ColorMap, uv).r;
    #endif

    //float smoothing = fwidth(dist); // faster
    float smoothing = 0.7 * length(vec2(dFdx(dist), dFdy(dist)));
    float alpha = smoothstep(glyph_center-smoothing, glyph_center+smoothing, dist);

    // Smooth
    vec3 glyph_color = vec3(1.0);
    color = vec4(glyph_color, alpha);

    #ifdef HAS_COLOR
        color *= m_Color;
    #endif

    #ifdef HAS_VERTEXCOLOR
        color *= vVertColor;
    #endif

    #ifdef DISCARD_ALPHA
        if(color.a < m_AlphaDiscardThreshold){
           discard;
        }
    #endif

    gl_FragColor = color;
    // Outline
    // float mu = smoothstep(outline_center-smoothing, outline_center+smoothing, dist);
    // vec3 rgb = mix(outline_color, glyph_color, mu);
    // gl_FragColor = vec4(rgb, max(alpha,mu));

    // Glow
    // vec3 rgb = mix(glow_color, glyph_color, alpha);
    // float mu = smoothstep(glyph_center, glow_center, sqrt(dist));
    // gl_FragColor = vec4(rgb, max(alpha,mu));

    // Glow + outline
    // vec3 rgb = mix(glow_color, glyph_color, alpha);
    // float mu = smoothstep(glyph_center, glow_center, sqrt(dist));
    // color = vec4(rgb, max(alpha,mu));
    // float beta = smoothstep(outline_center-smoothing, outline_center+smoothing, dist);
    // rgb = mix(outline_color, color.rgb, beta);
    // gl_FragColor = vec4(rgb, max(color.a,beta));
}