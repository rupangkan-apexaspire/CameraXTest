package com.example.cameraxtest.models

import androidx.lifecycle.ViewModel

class AppViewModel(): ViewModel() {

    // fileLists
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