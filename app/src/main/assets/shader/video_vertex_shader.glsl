#version 120
attribute vec4 vPosition;
attribute vec2 inputTextureCoordinate;
varying vec2 textureCoordinate;
void main(){
    gl_Position = vPosition;
    textureCoordinate = inputTextureCoordinate;
}
// 绘制纹理的shader需要顶点数据、纹理顶点数据和纹理
// vPosition 顶点数据。
// inputTextureCoordinate：纹理顶点数据。
// textureCoordinate：varying类型，textureCoordinate是inputTextureCoordinate的值，传递给Fragment Shader使用。
