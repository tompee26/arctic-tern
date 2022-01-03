package com.tompee.arctictern.compiler

import com.google.devtools.ksp.symbol.KSNode

class ProcessingException(message: String, val node: KSNode) : Exception(message)
