module Main exposing (..)

import Browser
import Html exposing (..)
import Http
import Json.Decode exposing (Decoder, list)
import Json.Decode as Decode exposing (Decoder)

-- MAIN

main =
  Browser.element
    { init = init,
    update = update,
    subscriptions = subscriptions,
    view = view
    }

-- MODEL

type Model
  = Failure
  | Loading
  | Success (List PlayerGames)


type alias PlayerGames =
  {
      matchTime : String,
      winnerName : String,
      winnerCharacters : List(String),
      loserName : String,
      loserCharacters : List(String)
  }

init : () -> (Model, Cmd Msg)
init _ =
  (Loading, getPlayerGames)

-- UPDATE

type Msg
  = GotPlayerGames (Result Http.Error (List PlayerGames))


update : Msg -> Model -> (Model, Cmd Msg)
update msg model =
  case msg of
    GotPlayerGames result ->
      case result of
        Ok games ->
          (Success games, Cmd.none)

        Err _ ->
          (Failure, Cmd.none)

-- SUBSCRIPTIONS

subscriptions : Model -> Sub Msg
subscriptions model =
  Sub.none

-- VIEW

view : Model -> Html Msg
view model =
  div []
    [ h2 [] [ text "Player Games" ]
    , viewPlayerGames model
    ]

viewPlayerGames : Model -> Html Msg
viewPlayerGames model =
  case model of
    Failure ->
      div []
        [ text "I could not load games for some reason. "
        ]

    Loading ->
      text "Loading..."

    Success games ->
      p [] [text games]

-- HTTP

getPlayerGames : Cmd Msg
getPlayerGames =
  Http.get
    { url = "/playerid/180905221050371465"
    , expect = Http.expectJson GotPlayerGames allGamesDecoder
    }
-- https://stackoverflow.com/questions/35028430/how-to-extract-the-results-of-http-requests-in-elm

playerGamesDecoder : Decoder PlayerGames
playerGamesDecoder =
  Decode.map5 PlayerGames
    (Decode.field "matchTime" Decode.string)
    (Decode.field "winnerName" Decode.string)
    (Decode.field "winnerCharacters" (Decode.list Decode.string))
    (Decode.field "loserName" Decode.string)
    (Decode.field "loserCharacters" (Decode.list Decode.string))


allGamesDecoder : Decoder (List PlayerGames)
allGamesDecoder =
    list playerGamesDecoder