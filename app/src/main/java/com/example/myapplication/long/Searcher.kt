package com.example.myapplication.long

import com.example.myapplication.long.model.StopOrLine

interface Searcher {
    //返回搜索结果
    fun search(name:String):List<StopOrLine>
}