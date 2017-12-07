package com.example.stobix.myapplication

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey


interface Containable

@Entity
data class Container(
        @PrimaryKey var containerID: Int,
        var contents: List<Containable>) : Containable

@Entity
class IntContainerEntry(
        @PrimaryKey var id: Int,
        var containerID: Int,
        var pos: Int,
        var thing: Int
) : Containable

@Entity
class StringContainerEntry(
        @PrimaryKey var id: Int,
        var containerID: Int,
        var pos: Int,
        var thing: String
) : Containable

@Entity
class PropertyContainerEntry(
        @PrimaryKey var id: Int,
        var containerID: Int,
        var pos: Int,
        var property: String,
        var amount: Int,
        var amountable: Boolean,
        var description: String?
) : Containable

@Entity
class ContainerContainerEntry(
        @PrimaryKey var id: Int,
        var containerID: Int,
        var pos: Int,
        var otherContainerID: Int
) : Containable


/*
 *
 *  [[Int,String],String,Property:Amount "Some thing"]
 *  ->
 *  Container CID
 *  ContainerContainerEntry X 0 CID
 *  IntContainerEntry _ X 0 Int
 *  StringContainerEntry _ X 1 String
 *  StringContainerEntry _ CID 1 String
 *  PropertyContainerEntry _ CID 2 Property Amount true "Some thing"
 *
 */
