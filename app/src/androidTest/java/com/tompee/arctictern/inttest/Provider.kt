package com.tompee.arctictern.inttest

class Provider<T>(private val value: T) {

    fun get(): T = value
}
