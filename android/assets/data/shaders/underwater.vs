attribute vec3 a_position;
uniform mat4 u_projViewTrans;
uniform mat4 u_worldTrans;
attribute vec2 a_texCoord0;
varying vec2 v_texCoords0;

attribute vec3 a_normal;
uniform mat3 u_normalMatrix;
varying vec3 v_normal;

void main()
{
    vec4 pos = u_worldTrans * vec4(a_position, 1.0);
    gl_Position =  u_projViewTrans * pos;

    v_texCoords0 = a_texCoord0;

    vec3 normal = normalize(u_normalMatrix * a_normal);
    v_normal = normal;

}