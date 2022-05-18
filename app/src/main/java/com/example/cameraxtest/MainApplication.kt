package com.example.cameraxtest

import android.app.Application

class MainApplication: Application() {
    var fileList: MutableList<String> = arrayListOf()

    fun setList() {
        fileList = ArrayList()
    }

    fun addToFileList(fileUri: String){
        fileList.add(fileUri)
    }

    fun getFileList2(): MutableList<String> {
        return fileList
    }

    fun deleteFromList(position: Int) {
        fileList.removeAt(position)
    }

}