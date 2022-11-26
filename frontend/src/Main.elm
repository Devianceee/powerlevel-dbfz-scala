module Main exposing (..)

import Browser
import Html exposing (Html, Attribute, div, input, text)
import Html.Attributes exposing (..)
import Html.Events exposing (onInput)

-- MAIN

main =
  Browser.sandbox { init = init, update = update, view = view }

-- MODEL

type alias Model = { content: Int }

init : Model
init = { content = 0 }

-- UPDATE

type Msg = Change Int

update : Msg -> Model -> Model
update msg model =
  case msg of
    Change newContent ->
        { model | content = newContent }

-- VIEW

view : Model -> Html Msg
view model =
  div []
    [ input [placeholder "Text to reverse", value model, onInput Change] []
    , div [] [text (model)]
    ]
