#version 120
#extension GL_OES_EGL_image_external : require
precision mediump float;
varying vec2 textureCoordinate;
uniform samplerExternalOES s_texture;
void main() {
    gl_FragColor = texture2D(s_texture, textureCoordinate);
}

// s_texture：纹理，其类型是samplerExternalOES。
// textureCoordinate：Vertex Shader传递过来的纹理顶点数据，texture2D是OpenGL ES内置函数，称之为采样器，获取纹理上指定位置的颜色值。
