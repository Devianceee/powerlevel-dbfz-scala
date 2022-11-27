module Main exposing (..)

import Browser
import Html exposing (..)
import Html.Attributes exposing (style)
import Html.Events exposing (..)
import Http
import Json.Decode exposing (Decoder, field, int, list, map4, map5, string)



-- MAIN


main =
  Browser.element
    { init = init
    , update = update
    , subscriptions = subscriptions
    , view = view
    }



-- MODEL


type Model
  = Failure
  | Loading
  | Success PlayerGames


type alias PlayerGames =
  { matchTime : String
  , winnerName : String
  , winnerCharacters : List(String)
  , loserName : String
  , loserCharacters : List(String)
  }


init : () -> (Model, Cmd Msg)
init _ =
  (Loading, getPlayerGames)



-- UPDATE


type Msg
  = GotPlayerGames (Result Http.Error PlayerGames)


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
      div []
        [
        blockquote [] [ text games.winnerName ]
        , p [ style "text-align" "right" ]
            [ text "— " ]
        ]



-- HTTP


getPlayerGames : Cmd Msg
getPlayerGames =
  Http.get
    { url = "/playerid/180905221050371465"
    , expect = Http.expectJson GotPlayerGames playerGamesDecoder
    }


playerGamesDecoder : Decoder PlayerGames
playerGamesDecoder =
  map5 PlayerGames
    (field "matchTime" string)
    (field "winnerName" string)
    (field "winnerCharacters" (list string))
    (field "loserName" string)
    (field "loserCharacters" (list string))