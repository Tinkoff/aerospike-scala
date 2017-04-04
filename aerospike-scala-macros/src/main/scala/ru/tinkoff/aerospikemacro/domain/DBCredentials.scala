package ru.tinkoff.aerospikemacro.domain

/**
  * @author MarinaSigaeva 
  * @since 22.11.16
  */
case class DBCredentials(namespace: String, setname: String)

trait WrapperException extends Exception { val msg: String }
