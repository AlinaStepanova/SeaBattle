package com.avs.sea.battle

const val NAMESPACE = "com.avs.sea.battle"
const val SQUARES_COUNT = 10
const val FOUR_DECK_SHIP_SIZE = 4
const val THREE_DECK_SHIP_SIZE = 3
const val TWO_DECK_SHIP_SIZE = 2
const val ONE_DECK_SHIP_SIZE = 1

// The fleet each player places, largest ship first.
val FLEET = listOf(
    FOUR_DECK_SHIP_SIZE, THREE_DECK_SHIP_SIZE, THREE_DECK_SHIP_SIZE,
    TWO_DECK_SHIP_SIZE, TWO_DECK_SHIP_SIZE, TWO_DECK_SHIP_SIZE,
    ONE_DECK_SHIP_SIZE, ONE_DECK_SHIP_SIZE, ONE_DECK_SHIP_SIZE, ONE_DECK_SHIP_SIZE,
)

const val SECOND_IN_MILLIS = 1000L
const val REJECTED_FLASH_MILLIS = 400L
const val SHARE_INTENT_TYPE = "text/plain"
const val DEV_NAME = "Alina Stepanova"
const val PRIVACY_POLICY_URL = "https://alinastepanova.github.io/SeaBattle/"
const val PLAY_MARKET_URL = "https://play.google.com/store/apps/details?id=$NAMESPACE"
const val RECIPIENTS = "alina.stepanova.work@gmail.com"
