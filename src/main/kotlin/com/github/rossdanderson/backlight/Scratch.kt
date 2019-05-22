package com.github.rossdanderson.backlight

//
///*
// __ __ __ __ __
//| 1| 2| 3| 4| 5|
//|__|  |  |  |__|
//|12|__|__|__| 6|
//|__|10| 9| 8|__|
//|11|  |  |  | 7|
//|__|__|__|__|__|
// */
//
//
//
//val sideRowWidth = width / 4
//val midRowWidth = width / 4 * 2 / 3
//val sideRowHeight = height / 3
//val midRowHeight = height / 2
//
//val screenSections = listOf(
//    IntRange2D(
//        xRange = offsetIntRange(0, sideRowWidth),
//        yRange = offsetIntRange(0, sideRowHeight)
//    ),
//    IntRange2D(
//        xRange = offsetIntRange(sideRowWidth, midRowWidth),
//        yRange = offsetIntRange(0, midRowHeight)
//    ),
//    IntRange2D(
//        xRange = offsetIntRange(sideRowWidth + midRowWidth, midRowWidth),
//        yRange = offsetIntRange(0, midRowHeight)
//    ),
//    IntRange2D(
//        xRange = offsetIntRange(sideRowWidth + midRowWidth + midRowWidth, midRowWidth),
//        yRange = offsetIntRange(0, midRowHeight)
//    ),
//    IntRange2D(
//        xRange = offsetIntRange(sideRowWidth + midRowWidth + midRowWidth + midRowWidth, sideRowWidth),
//        yRange = offsetIntRange(0, sideRowHeight)
//    ),
//    IntRange2D(
//        xRange = offsetIntRange(sideRowWidth + midRowWidth + midRowWidth + midRowWidth, sideRowWidth),
//        yRange = offsetIntRange(sideRowHeight, sideRowHeight)
//    ),
//    IntRange2D(
//        xRange = offsetIntRange(sideRowWidth + midRowWidth + midRowWidth + midRowWidth, sideRowWidth),
//        yRange = offsetIntRange(sideRowHeight + sideRowHeight, sideRowHeight)
//    ),
//    IntRange2D(
//        xRange = offsetIntRange(sideRowWidth + midRowWidth + midRowWidth, midRowWidth),
//        yRange = offsetIntRange(midRowHeight, midRowHeight)
//    ),
//    IntRange2D(
//        xRange = offsetIntRange(sideRowWidth + midRowWidth, midRowWidth),
//        yRange = offsetIntRange(midRowHeight, midRowHeight)
//    ),
//    IntRange2D(
//        xRange = offsetIntRange(sideRowWidth, midRowWidth),
//        yRange = offsetIntRange(midRowHeight, midRowHeight)
//    ),
//    IntRange2D(
//        xRange = offsetIntRange(0, sideRowWidth),
//        yRange = offsetIntRange(sideRowHeight + sideRowHeight, sideRowHeight)
//    ),
//    IntRange2D(
//        xRange = offsetIntRange(0, sideRowWidth),
//        yRange = offsetIntRange(sideRowHeight, sideRowHeight)
//    )
//)