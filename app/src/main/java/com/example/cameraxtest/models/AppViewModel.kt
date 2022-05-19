package com.example.cameraxtest.models

import androidx.lifecycle.ViewModel

class AppViewModel(): ViewModel() {

    var fileList: MutableList<String> = arrayListOf()
    var title: String = ""
    var description: String = ""
    var genderCheckedId: Int? = null
    var standardCheckedId: Int? = null
    var interests: String = ""

//    fun setDescription(description: String) {
//        this.description = description
//    }
//
//    fun getDescription(): String {
//        return description
//    }
//
//    fun setTitle(title: String) {
//        this.title = title
//    }
//
//    fun getTitle(): String {
//        return title
//    }

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