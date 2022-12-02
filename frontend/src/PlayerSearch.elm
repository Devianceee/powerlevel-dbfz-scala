module PlayerSearch exposing (..)

import Browser
import Html exposing (..)
import Html.Events exposing (onClick, onInput)
import Http
import Json.Decode exposing (Decoder, decodeString, list, string)
import Json.Decode as Decode exposing (Decoder)
import Html.Attributes exposing (datetime)
import Html.Styled.Attributes exposing (css, href, src)
import Time

import Bootstrap.CDN as CDN
import Bootstrap.Grid as Grid
import Bootstrap.Navbar as Navbar

import Html.Attributes exposing (class)

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
    , glickoValue: Int
    , glickoDeviation: Int
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
    { url = "/api/search?name=" ++ query
    , expect = Http.expectJson GotResponse searchResultListDecoder
    }

searchResultDecoder : Decoder SearchResult
searchResultDecoder =
  Decode.map5 SearchResult
    (Decode.field "uniquePlayerID" Decode.string)
    (Decode.field "name" Decode.string)
    (Decode.field "latestMatchTime" Decode.string)
    (Decode.field "glickoValue" Decode.int)
    (Decode.field "glickoDeviation" Decode.int)

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
  Grid.container[] [
    div [] 
    [ CDN.stylesheet,
      div [class "jumbotron jumbotron-fluid text-center"] 
      [
        h1 [] [ text "Powerlevel - DBFZ Rating System" ]
        , h3 [] [ text "Currently in beta" ]
        , p [] [ text "Game results may be deleted without notice until out of beta" ]
      ]
    , div [class "navbar navbar-expand-sm justify-content-center"] 
    [
      input [ onInput UpdateQuery ] [ text (String.toLower model.query) ]
      , button [ onClick SendRequest ] [ text "Search" ]
      
    ]
    , div [] 
      [
        h5 [class "d-flex justify-content-center"] [ text "Copy your ID and put it at the end of: http://powerlevel.info/api/playerid/" ]
      ]
    , viewOutcome model.outcome
    ]
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
            [ th [] [ text "ID" ]
            , th [] [ text "Player name" ]
            , th [] [ text "Latest match time" ]
            , th [] [ text "Rating value" ]
            ]
        renderResult: SearchResult -> Html Msg
        renderResult = \result -> tr []
        
          [ td [] [ text result.uniquePlayerID ]
          , td [] [ text result.name ]
          , td [] [ text result.latestMatchTime ]
          , td [] [ text ( (String.fromInt result.glickoValue) ++ " ± " ++ (String.fromInt result.glickoDeviation)) ]
          ]
      in
      table [class "table table-responsive w-100 d-block d-md-table"] (tableHeaders :: (List.map renderResult searchResults))
    
