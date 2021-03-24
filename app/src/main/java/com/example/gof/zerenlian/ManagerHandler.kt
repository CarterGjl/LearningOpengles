package com.example.gof.zerenlian

import java.math.BigDecimal

class ManagerHandler : Handler {
    override fun process(request: Request): Boolean? {
        if (request.amount > BigDecimal.valueOf(1000)){
            return null
        }
        return request.name != "bob"
    }
}