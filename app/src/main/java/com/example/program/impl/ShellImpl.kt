package com.example.program.impl

import com.example.program.IShellMethod
import com.example.program.IUseShell

class ShellImpl : IShellMethod,IUseShell {

    override fun autoAttack() {
        println("auto attack hero or NPC")
    }

    override fun stopAutoAttack() {
        println("stop auto attack")
    }

    private fun calculateDuckingPath() {
        println("calculate best ducking path!")
    }

    override fun autoDucking() {
        calculateDuckingPath();
        println("auto dodge attacks")
    }

    override fun stopAutoDucking() {
        println("stop auto dodge attacks")
    }

    override fun autoUseSkill() {
        println("auto use hero's skill")
    }

    override fun stopAutoUseSkill() {
        println("stop auto use hero's skill")
    }

    override fun autoChangeChangeEquipage() {
        println("auto buy best equipage")
    }

    override fun stopAutoChangeChangeEquipage() {
        println("stop auto buy best equipage")
    }

    override fun enableShell() {
        this.autoAttack();
        this.autoDucking();
        this.autoUseSkill();
        this.autoChangeChangeEquipage();
    }

    override fun disableShell() {
        this.stopAutoAttack();
        this.stopAutoDucking();
        this.stopAutoUseSkill();
        this.stopAutoChangeChangeEquipage();
    }
}