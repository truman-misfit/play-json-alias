package com.truman.utils

import scala.annotation.tailrec

object Base62Encoder {
  private val Characters = (('0' to '9') ++ ('a' to 'z') ++ ('A' to 'Z')).mkString
  private val Base = BigInt(Characters.length)

  /**
   * Encodes a positive Base 10 number into a Base 62 String.
   *
   * @param num The number to encode
   * @return The number as a Base 62 String.
   * @throws IllegalArgumentException if the number is negative.
   */
  def encode(num: BigInt): String = {
    if (num < BigInt(0)) {
      throw new IllegalArgumentException("Can't encode negative numbers")
    }

    @tailrec
    def makeBase62Digits(i: BigInt, acc: List[Int]): List[Int] = {
      val div = i / Base
      val rem = (i % Base).toInt
      if (div == BigInt(0)) {
        rem :: acc
      } else {
        makeBase62Digits(div, rem :: acc)
      }
    }

    makeBase62Digits(num, Nil).map(Characters.charAt).mkString
  }

  /**
   * Decodes a Base 62 String into a positive Base 10 number.
   *
   * @param str The String to decode
   * @return The Base 10 number that the String represents.
   * @throws IllegalArgumentException if the String is empty or contains a non-Base 62 character
   */
   def decode(str: String): BigInt = {
     if (str.isEmpty) {
       throw new IllegalArgumentException("Can't decode an empty string")
     }

     str.zip(str.indices.reverse).foldLeft(BigInt(0)) { (result, charAndPower) =>
       val (char, power) = charAndPower
       val charValue = Characters.indexOf(char)
       if (charValue < 0) {
         throw new IllegalArgumentException(s"Invalid character: $char")
       } else {
         result + (Base.pow(power) * BigInt(charValue))
       }
     }
   }
}
