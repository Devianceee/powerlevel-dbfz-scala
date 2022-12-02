module PlayerSearch exposing (..)

import Browser
import Html exposing (..)
import Html.Events exposing (onClick, onInput)
import Http
import Json.Decode exposing (Decoder, decodeString, list, string)
import Json.Decode as Decode exposing (Decoder)


-- MAIN

main =
  Browser.element
    { init = init
    , update = update
    , subscriptions = subscriptions
    , view = view
    }


-- MODEL

type alias Model =
  { query: String
  , outcome: SearchOutcome
  }

type SearchOutcome
  = Failure
  | Loading
  | Success (List SearchResult)

type alias SearchResult =
    { uniquePlayerID: String
    , name: String
    , latestMatchTime: String
    }

init : () -> (Model, Cmd Msg)
init _ =
  ( Model "" (Success [])
  , Cmd.none
  )


-- UPDATE

type Msg
  = UpdateQuery String
  | SendRequest
  | GotResponse (Result Http.Error (List SearchResult))

update : Msg -> Model -> (Model, Cmd Msg)
update msg model =
  case msg of
    UpdateQuery query ->
      ( { model | query = query }
      , Cmd.none
      )

    SendRequest ->
      ( model
      , makeSearchRequest model.query
      )
    
    GotResponse result ->
      case result of
        Err _ ->
          ( { model | outcome = Failure }
          , Cmd.none
          )
        
        Ok searchResults ->
          ( { model | outcome = Success searchResults }
          , Cmd.none
          )


makeSearchRequest : String -> Cmd Msg
makeSearchRequest query =
  let
    _ = Debug.log "Issuing the following search query: " query
  in
  Http.get
    { url = "/search?name=" ++ query
    , expect = Http.expectJson GotResponse searchResultListDecoder
    }

searchResultDecoder : Decoder SearchResult
searchResultDecoder =
  Decode.map3 SearchResult
    (Decode.field "uniquePlayerID" Decode.string)
    (Decode.field "name" Decode.string)
    (Decode.field "latestMatchTime" Decode.string)

searchResultListDecoder : Decoder (List SearchResult)
searchResultListDecoder =
    list searchResultDecoder


-- SUBSCRIPTIONS

subscriptions : Model -> Sub Msg
subscriptions model =
  Sub.none


-- VIEW

view : Model -> Html Msg
view model =
  div []
    [ input [ onInput UpdateQuery ] [ text model.query ]
    , button [ onClick SendRequest ] [ text "Search" ]
    , viewOutcome model.outcome
    ]

viewOutcome : SearchOutcome -> Html Msg
viewOutcome outcome =
  case outcome of
    Failure ->
      text "Failed to load search results"

    Loading ->
      text "Loading"

    Success searchResults ->
      let
        tableHeaders = 
          thead []
            [ th [] [ text "id" ]
            , th [] [ text "name" ]
            , th [] [ text "latest match time" ]
            ]
        renderResult: SearchResult -> Html Msg
        renderResult = \result -> tr []
          [ td [] [ text result.uniquePlayerID ]
          , td [] [ text result.name ]
          , td [] [ text result.latestMatchTime ]
          ]
      in
      table [] (tableHeaders :: (List.map renderResult searchResults))
