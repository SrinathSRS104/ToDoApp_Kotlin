package com.example.todo

class DbModel {
        companion object Factory {                          // SRS: Something related to static behaviour for easy access
            fun create(): DbModel = DbModel()
        }
        var objectId: String? = null
        var itemText: String? = null
        var done: Boolean? = false
    }