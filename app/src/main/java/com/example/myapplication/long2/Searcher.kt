package com.example.myapplication.long2

import com.example.myapplication.long2.model.StopOrLine

interface Searcher {
    //返回搜索结果
    fun search(name:String):List<StopOrLine>
}