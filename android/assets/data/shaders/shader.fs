#ifdef GL_ES
#define LOWP lowp
#define MED mediump
#define HIGH highp
precision mediump float;
#else
#define MED
#define LOWP
#define HIGH
#endif

varying MED vec2 v_texCoords0;
uniform sampler2D u_normalTexture;
uniform sampler2D u_diffuseTexture;

varying vec3 v_normal;

uniform vec4 u_fogColor;
varying float v_fog;

varying float baselight;
varying vec3 blocklight;

void main()
{
    vec3 light = blocklight.rgb+baselight;
        if (v_normal.x < 0.0 || v_normal.x >0.0){
            light -= 0.1;
        }
        if (v_normal.z < 0.0 || v_normal.z >0.0){
            light -= 0.05;
        }
        if (v_normal.y < 0.0){
            light -= 0.1;
        }

    vec4 texColor = texture2D(u_diffuseTexture, v_texCoords0.xy).rgba;
    vec4 finalColor = vec4(texColor.xyz * light.rgb, texColor.a);

    const float LOG2 = 1.442695;
    float z = (gl_FragCoord.z / gl_FragCoord.w)/4.0;
    float fogFactor = exp2( -0.02 * 0.02 * z * z * LOG2 );
    fogFactor = clamp(fogFactor, 0.0, 1.0);

    gl_FragColor = mix(u_fogColor, finalColor, fogFactor );

}

