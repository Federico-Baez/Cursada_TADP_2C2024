import Parsers.integer

import scala.collection.mutable.ListBuffer
import scala.util.boundary.break

package object Parsers {

  import scala.util.{Failure, Success, Try}

  import scalafx.scene.input.KeyCode.A

  import scala.util.{Failure, Success, Try}


  val anyChar: Parser[Char] = Parser { textoParsear =>
    if (textoParsear.isEmpty) {
      Failure(new IllegalArgumentException("La cadena está en blanco"))
    } else {
      // Esto no seria un Success?
      Success((textoParsear.head, textoParsear.tail))
    }
  }


  def char(caracterEsperado: Char): Parser[Char] = Parser { textoParsear =>
    if (textoParsear.nonEmpty && textoParsear.head == caracterEsperado) {
      Try(caracterEsperado, textoParsear.tail)
    } else {
      Failure(new IllegalArgumentException(s"El texto '$textoParsear' no comienza con '$caracterEsperado'"))
    }
  }


  def string(stringEsperado: String): Parser[String] = Parser { stringParsear =>
    if (stringParsear.startsWith(stringEsperado)) {
      Success((stringEsperado, stringParsear.substring(stringEsperado.length)))
    } else {
      Failure(new IllegalArgumentException(s"El string '$stringParsear' no contiene '$stringEsperado' al principio."))
    }
  }


  val digit: Parser[Char] = Parser { stringParsear =>
    if (stringParsear.nonEmpty && stringParsear.head.isDigit) {
      Success((stringParsear.head, stringParsear.tail))
    } else {
      Failure(new IllegalArgumentException(s"El carácter '${stringParsear.headOption.getOrElse("")}' no es un dígito"))
    }
  }


  val double: Parser[Double] = Parser { stringParsear =>
    // Verificar si el número es negativo
    val esNegativo = stringParsear.startsWith("-")

    // Si es negativo, eliminar el signo "-" y procesar el resto de la cadena
    val digitos = if (esNegativo) stringParsear.tail else stringParsear

    // Usar span para extraer la parte entera antes del punto
    val (parteEntera, resto1) = digitos.span(_.isDigit)

    // Verificar si hay un punto decimal
    if (resto1.startsWith(".")) {
      // Extraer la parte decimal después del punto
      val (parteDecimal, resto2) = resto1.tail.span(_.isDigit)

      // Si no hay parte decimal, lanzar un error
      if (parteDecimal.isEmpty) {
        Failure(new IllegalArgumentException(s"El string '$stringParsear' no es un número decimal válido"))
      } else {
        // Construir el número decimal (parte entera + punto + parte decimal)
        val numeroString = parteEntera + "." + parteDecimal

        // Convertir el número string a Double
        Try((if (esNegativo) -numeroString.toDouble else numeroString.toDouble, resto2))
      }
    } else if (parteEntera.isEmpty) {
      // Si no se encontró ninguna parte entera ni decimal, lanzar un error
      Failure(new IllegalArgumentException(s"El string '$stringParsear' no contiene un número válido"))
    } else {
      // Si no hay punto decimal, interpretarlo como un número entero
      Try((if (esNegativo) -parteEntera.toDouble else parteEntera.toDouble, resto1))
    }
  }


  val integer: Parser[Int] = Parser { stringParsear =>
    // Verificar si el número es negativo
    val esNegativo = stringParsear.startsWith("-")

    // Si es negativo, eliminar el signo "-" y procesar el resto de la cadena
    val digitos = if (esNegativo) stringParsear.tail else stringParsear

    // Usar span para extraer los dígitos
    val (numeroString, resto) = digitos.span(_.isDigit)

    // Si no hay dígitos, lanzar un error
    if (numeroString.isEmpty) {
      Failure(new IllegalArgumentException(s"El string '$stringParsear' no contiene un número válido"))
    } else {
      // Convertir los dígitos a un número entero y devolver el resultado
      Try((if (esNegativo) -numeroString.toInt else numeroString.toInt, resto))
    }
  }


  case class Parser[+A](funcionParser: String => Try[(A, String)]) {

    def map[B](f: A => B): Parser[B] = Parser { input =>
      funcionParser(input).map { (result, rest) => (f(result), rest) }
    }

    def flatMap[B](f: A => Parser[B]): Parser[B] = Parser { input =>
      funcionParser(input).flatMap { (result, rest) => f(result).funcionParser(rest) }
    }

    def <|>[B >: A](otroParser: => Parser[B]): Parser[B] = Parser { string =>
      funcionParser(string).recoverWith(_ => otroParser.funcionParser(string))
    }


    def <>[B](otroParser: => Parser[B]): Parser[(A, B)] = for {
      resultParser <- this
      resultOtroParser <- otroParser
    } yield (resultParser, resultOtroParser)

    def ~>[B](otroParser: => Parser[B]): Parser[B] = for {
      _ <- this
      resultadoOtroParser <- otroParser
    } yield resultadoOtroParser

    def <~[B](otroParser: => Parser[B]): Parser[A] = for {
      resultado <- this
      _ <- otroParser
    } yield resultado

    def sepBy[B](parserSeparador: Parser[B]): Parser[List[A]] = for {
      resultado <- this
      resto <- (parserSeparador ~> this).*
    } yield resultado :: resto

    def satisfies(unaCondicion: A => Boolean): Parser[A] = Parser { string =>
      funcionParser(string).flatMap((resultado, resto) => if (unaCondicion(resultado)) Success(resultado, resto) else Failure(new IllegalArgumentException("el resultado no cumple la operacion")))
    }

    def opt: Parser[Option[A]] = Parser { string =>
      funcionParser(string).fold(_ => Success(None, string), (resultado, sobrante) => Success(Some(resultado), sobrante))
    }

    def * : Parser[List[A]] = Parser { string =>
      this.+.funcionParser(string).recover { _ => (List.empty[A], string) }
    }

    def + : Parser[List[A]] = for {
      resultado <- this
      resto <- this.*
    } yield resultado :: resto

  }


  //-----------------------------------------------------PRUEBAS--------------------------------------------

  //
  //  val holaMundo = string("hola") <> string("mundo")
  //  print(holaMundo.funcionParser("holamundocomoandas"))
  //  print("\n")
  //  val holaMundo2 = string("hola") ~> string("mundo")
  //  print(holaMundo2.funcionParser("holamundocomoandas"))
  //  print("\n")
  //  val holaMundo3 = string("hola") <~ string("mundo")
  //  print(holaMundo3.funcionParser("holamundocomoandas"))
  //  print("\n")
  //  val holaMundo4 = string("hola").sepBy(char('-'))
  //  print(holaMundo4.funcionParser("hola-hola1109"))
  //  print("\n")
  //  val holaMundo5 = string("hola").sepBy(char('5'))
  //  print(holaMundo5.funcionParser("hola5hola1109"))
  //  print("\n")
  //  val numeroDeTelefono = integer <~ char('-') //integer().sepBy(char('-'))
  //  print(numeroDeTelefono.funcionParser("4356-1234"))
  //  print(("\n"))
  //  print("Parsear un string con digit deberia fallar: \n")
  //  val parserDigit = digit
  //  print(parserDigit.funcionParser("stringLoco"))
  //  val parserInteger = integer
  //  print(parserInteger.funcionParser("3423545"))
  //  print("\n")
  //  print(double.funcionParser("-545345.54"))
  //  print("\n")
  //
  //  print("\n")
  //  print(string("hola").opt.funcionParser("hola"))
  //  print("\n")
  //  print(string("hola").opt.funcionParser("comoestas"))
  //  print("\n")


}

object Testear extends App {
  print(integer.satisfies(unNumero => unNumero == 3).funcionParser("2"))
}
