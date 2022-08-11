#version 120
#extension GL_OES_EGL_image_external : require
attribute vec4 vPosition;
uniform mat4 vMatrix;
attribute vec2 vCoordinate;
varying vec2 textureCoordinate;
void main() {
    gl_Position =vMatrix * vPosition;
}