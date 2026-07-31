package com.example.cursotestingandroid.core.mothers

import com.example.cursotestingandroid.core.builders.product

object ProductMother {

    fun bread(stock:Int = 8) = product { withId("idBread"); withName("Pan"); withCategory("bread"); withPrice(2.50); withStock(stock) }
    fun milk(stock:Int = 3) = product { withId("idMilk"); withName("Leche"); withCategory("Lacteo"); withPrice(1.50); withStock(stock) }
    fun coffee(stock:Int = 2) = product { withId("idCoffee"); withName("Cafe"); withCategory("Drinks"); withPrice(3.00); withStock(stock) }

}