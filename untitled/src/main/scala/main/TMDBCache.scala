package main

import request.{RequestJSONCache, RequestObjectInCache}
import data.{Cache,FullName}

object TMDBCache extends App {
  val apiKey = "87551e2a0dd79b7a73f084bb30486087"
  val path_cache = "./src/main/scala/cache"
  val path_cache2 = "./src/main/scala/cache2"
  val cache = Cache(path_cache2)
  val user = RequestJSONCache(apiKey, path_cache)
  val user2 = RequestObjectInCache(apiKey, cache)

  //Test findActorId
  println("------------------ Test findActorId ------------------")
  println()
  println("########## Avec Request JSON ##########")
  println("On cherche l'id de Will Smith ==> Pas de cache")
  val idWill = user.findActorId("Will", "Smith")
  idWill match {
    case None => println("Aucun id n'a été trouvé")
    case Some(value) => println("id Will Smith = " + value)
  }

  println("On le cherche de nouveau ==> Cache primaire")
  val idWill2 = user.findActorId("Will", "Smith")
  idWill2 match {
    case None => println("Aucun id n'a été trouvé")
    case Some(value) => println("id Will Smith = " + value)
  }

  println("On cherche l'id de Emma Watson ==> Cache Secondaire")
  val idEmma = user.findActorId("Emma", "Watson")
  idEmma match {
    case None => println("Aucun id n'a été trouvé")
    case Some(value) => println("id Emma Watson = " + value)
  }
  println()
  println("########## Avec Request Cache class ##########")

  println("On cherche l'id de Benedict Cumberbatch ==> Pas de cache")
  val idBene = user2.findActorId("Benedict", "Cumberbatch")
  idBene match {
    case None => println("Aucun id n'a été trouvé")
    case Some(value) => println("id Benedict Cumberbatch = " + value)
  }

  println("On le cherche de nouveau ==> Cache primaire")
  val idBene2 = user2.findActorId("Benedict", "Cumberbatch")
  idBene2 match {
    case None => println("Aucun id n'a été trouvé")
    case Some(value) => println("id Benedict Cumberbatch = " + value)
  }

  println("On cherche l'id de Emma Watson ==> Cache Secondaire")
  val idEmma2 = user2.findActorId("Emma", "Watson")
  idEmma2 match {
    case None => println("Aucun id n'a été trouvé")
    case Some(value) => println("id Emma Watson = " + value)
  }
  println()
  println()

  println("------------------ Test findActorMovies ------------------")
  println()
  println("########## Avec Request JSON ##########")
  println("On cherche l'id de Will Smith ==> Pas de cache")
  val movieWillCache = user.findActorMovies(2888)
  movieWillCache.isEmpty match {
    case true => println("Aucun film n'a été trouvé")
    case false => println("films de  Will Smith = " + movieWillCache)
  }

  println("On le cherche de nouveau ==> Cache primaire")
  val movieWillCache2 = user.findActorMovies(2888)
  movieWillCache2.isEmpty match {
    case true => println("Aucun film n'a été trouvé")
    case false => println("films de  Will Smith = " + movieWillCache2)
  }

  println("On cherche l'id de Emma Watson ==> Cache Secondaire")
  val movieEmmaCache = user.findActorMovies(10990)
  movieEmmaCache.isEmpty match {
    case true => println("Aucun film n'a été trouvé")
    case false => println("films de  Emma Watson = " + movieEmmaCache)
  }

  println()
  println("########## Avec Request Cache Object ##########")
  println("On cherche l'id de Will Smith ==> Pas de cache")
  val movieWill = user2.findActorMovies(2888)
  movieWill.isEmpty match {
    case true => println("Aucun film n'a été trouvé")
    case false => println("films de  Will Smith = " + movieWill)
  }

  println("On le cherche de nouveau ==> Cache primaire")
  val movieWill2 = user2.findActorMovies(2888)
  movieWill2.isEmpty match {
    case true => println("Aucun film n'a été trouvé")
    case false => println("films de  Will Smith = " + movieWill2)
  }

  println("On cherche l'id de Emma Watson ==> Cache Secondaire")
  val movieEmma = user2.findActorMovies(10990)
  movieEmma.isEmpty match {
    case true => println("Aucun film n'a été trouvé")
    case false => println("films de  Emma Watson = " + movieEmma)
  }
  println()
  println()

  println("------------------ Test findMovieDirector ------------------")
  println()
  println("########## Avec Request JSON ##########")
  println("On cherche le director de Mars Attack ==> Pas de cache")
  val directorMarsAttaque = user.findMovieDirector(1571)
  directorMarsAttaque match {
    case None => println("Aucun film n'a été trouvé")
    case Some(value) => println("director de Mars Attack = " + value)
  }

  println("On le cherche de nouveau ==> Cache primaire")
  val directorMarsAttaque2 = user.findMovieDirector(1571)
  directorMarsAttaque2 match {
    case None => println("Aucun film n'a été trouvé")
    case Some(value) => println("films de  Will Smith = " + value)
  }

  println("On cherche le director de Titanic ==> Cache Secondaire")
  val directorTitanic = user.findMovieDirector(597)
  directorTitanic match {
    case None => println("Aucun film n'a été trouvé")
    case Some(value) => println("Directeur de Titanic = " + value)
  }

  println()
  println("########## Avec Request Cache Object ##########")
  println("On cherche le director de Mars Attack ==> Pas de cache")
  val directorMarsAttaqueCache = user2.findMovieDirector(1571)
  directorMarsAttaqueCache match {
    case None => println("Aucun film n'a été trouvé")
    case Some(value) => println("director de Mars Attack = " + value)
  }

  println("On le cherche de nouveau ==> Cache primaire")
  val directorMarsAttaqueCache2 = user2.findMovieDirector(1571)
  directorMarsAttaqueCache2 match {
    case None => println("Aucun film n'a été trouvé")
    case Some(value) => println("films de  Will Smith = " + value)
  }

  println("On cherche le director de Titanic ==> Cache Secondaire")
  val directorTitanicCache = user2.findMovieDirector(597)
  directorTitanicCache match {
    case None => println("Aucun film n'a été trouvé")
    case Some(value) => println("Directeur de Titanic = " + value)
  }

  println("------------------ Test collaboration ------------------")
  val actor1 = FullName("Tom", "Holland")
  val actor2 = FullName("Andrew", "Garfield")
  println()
  println("########## Avec Request JSON ##########")
  val collaboration = user.collaboration(actor1, actor2)
  println()
  collaboration.isEmpty match {
    case true => println("Aucun film en commun")
    case false => println("Les films où les deux acteurs ont joué ensembles sont : " + collaboration.mkString(","))
  }
  println("########## Avec Request Cache Object ##########")
  val collaboration2 = user2.collaboration(actor1, actor2)
  println()
  collaboration2.isEmpty match {
    case true => println("Aucun film en commun")
    case false => println("Les films où les deux acteurs ont joué ensembles sont : " + collaboration2.mkString(","))
  }
}
