module Main exposing (..)

import Browser
import Html exposing (..)
import Http
import Json.Decode exposing (Decoder, decodeString, list, string)
import Json.Decode as Decode exposing (Decoder)
import Debug exposing (toString)
import String exposing (join)

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
      winnerCharacters : List String,
      loserName : String,
      loserCharacters : List String
  }

type alias SearchResult =
    { uniquePlayerID: String
    , name: String
    , latestMatchTime: String
    }

init : () -> (Model, String -> Cmd Msg)
init _ =
  (Loading, getPlayerGames)

-- UPDATE

type Msg
  = GotPlayerGames (Result Http.Error (List PlayerGames))
  | GotSearchResults (Result Http.Error (List SearchResult))


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
        [ text "I could not load games for some reason"]

    Loading ->
      text "Loading..."

    Success games ->
      let
        _ = games

        makeRow: PlayerGames -> Html Msg
        makeRow =
          \game -> tr []
          [ td [] [ text game.matchTime ]
          , td [] [ text game.winnerName ], td [] [ text (join ", " game.winnerCharacters) ]
          , td [] [ text game.loserName ], td [] [ text (join ", " game.loserCharacters) ]
          ]
      in
      table []
        (
            thead [] [ th[] [text "Match Time"], th[] [text "Winner"], th[] [text "Characters"], th[] [text "Loser"], th[] [text "Characters"]] ::
            (List.map makeRow games)
        )

-- HTTP

searchPlayers : String -> Cmd Msg
searchPlayers searchQuery =
    Http.get
        { url = "search?name=" + searchQuery
        , expect = Http.expectJson GotSearchResults ???
        }

getPlayerGames : String -> Cmd Msg
getPlayerGames id =
  Http.get
    { url = "/playerid/" ++ id
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


