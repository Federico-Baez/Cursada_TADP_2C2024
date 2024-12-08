import scalafx.scene.paint.Color
import tadp.drawing.TADPDrawingAdapter
import Parsers.*

import scala.util.Try

package object ParserImagenes {

  trait Figura {
    def print(adapter: TADPDrawingAdapter): Unit

    val getFiguraInterna: Figura = this match {
      case ColorFigura((_, _, _), figura) => figura
      case Rotacion(_, figura) => figura
      case Escala(_, _, figura) => figura
      case Traslacion(_, _, figura) => figura
      case figura => figura
    }

  }

  trait Metamorfosis extends Figura {
    def esNula: Boolean

    def aplicarA(figura: Figura): Figura

    def esIgualA(figuras: List[Figura]): Boolean = figuras.forall(esIgualA)

    def esIgualA(otra: Figura): Boolean
  }



  val parserCoordenadas: Parser[List[Double]] = char('[') ~> (double.sepBy(string(" @ ")) <~ string(", ").opt) <~ char(']')

  // Finalmente, el parser generico de formas devuelve una tres-upla compuesta por (forma, listaPuntos, radio?) donde radio es Some si la forma es un circulo, y en otro caso es none.
  def parserGenericoCoordenadas: Parser[(List[List[Double]], Option[Double])] = (char('[') ~> parserCoordenadas.+ <> double.opt) <~ char(']')

  def parserTipoFormaBasica = string("circulo") <|> string("rectangulo") <|> string("triangulo")

  type coordenada = (Double, Double)

  case class Triangulo(vertice1: coordenada,vertice2:coordenada,vertice3:coordenada) extends Figura {
    override def print(adapter: TADPDrawingAdapter): Unit =
      adapter.triangle(vertice1,vertice2,vertice3)
  }

  case class Rectangulo(vertice1: coordenada,vertice2:coordenada) extends Figura {
    override def print(adapter: TADPDrawingAdapter): Unit = {
      adapter.rectangle(vertice1,vertice2)
    }
  }

  case class Circulo(centro: coordenada,radio: Double) extends Figura {
    override def print(adapter: TADPDrawingAdapter): Unit = {
      adapter.circle(centro,radio)
    }
  }

  case class Grupo(figuras: List[Figura]) extends Figura {
    override def print(adapter: TADPDrawingAdapter): Unit = {
      figuras.foreach(figura => figura.print(adapter))
    }
  }

  case class ColorFigura(RGB: (Int, Int, Int), figura: Figura) extends Metamorfosis {
    override def print(adapter: TADPDrawingAdapter): Unit = {
      adapter.beginColor(Color.rgb(RGB._1, RGB._2, RGB._3))
      figura.print(adapter)
    }

    override def esNula: Boolean = false

    override def aplicarA(figura: Figura): Figura = ColorFigura(RGB, figura)

    override def esIgualA(otra: Figura): Boolean = otra match {
      case ColorFigura(rgb, _) => rgb == RGB
      case _ => false
    }

    def combinar(otra: ColorFigura): ColorFigura = otra
  }

  case class Escala(valor1: Double, valor2: Double, figura: Figura) extends Metamorfosis {
    override def print(adapter: TADPDrawingAdapter): Unit = {
      adapter.beginScale(valor1, valor2)
      figura.print(adapter)
    }

    override def esNula: Boolean = valor1 == 1 && valor2 == 1

    override def aplicarA(figura: Figura): Figura = Escala(valor1, valor2, figura)

    override def esIgualA(otra: Figura): Boolean = otra match {
      case Escala(fX, fY, _) => valor1 == fX && valor2 == fY
      case _ => false
    }

    def combinar(otra: Escala): Escala =
      Escala(valor1 * otra.valor1, valor2 * otra.valor2, otra.figura)
  }

  case class Rotacion(valor: Double, figura: Figura) extends Metamorfosis {
    override def print(adapter: TADPDrawingAdapter): Unit = {
      adapter.beginRotate(valor)
      figura.print(adapter)
    }

    override def esNula: Boolean = valor == 0

    override def aplicarA(figura: Figura): Figura = Rotacion(valor, figura)

    override def esIgualA(otra: Figura): Boolean = otra match {
      case Rotacion(g, _) => valor == g
      case _ => false
    }

    def combinar(otra: Rotacion): Rotacion =
      Rotacion((valor + otra.valor) % 360, otra.figura)
  }

  case class Traslacion(valor1: Double, valor2: Double, figura: Figura) extends Metamorfosis {
    override def print(adapter: TADPDrawingAdapter): Unit = {
      adapter.beginTranslate(valor1, valor2)
      figura.print(adapter)
    }

    override def esNula: Boolean = valor1 == 0 && valor2 == 0

    override def aplicarA(figura: Figura): Figura = Traslacion(valor1, valor2, figura)

    override def esIgualA(otra: Figura): Boolean = otra match {
      case Traslacion(dX, dY, _) => valor1 == dX && valor2 == dY
      case _ => false
    }
    def combinar(otra: Traslacion): Traslacion =
      Traslacion(valor1 + otra.valor1, valor2 + otra.valor2, otra.figura)
  }

  object parserFigurasRenovado {

    import Parsers._

    private lazy val figuraSimple: Parser[Figura] = triangulo <|> rectangulo <|> circulo

    private lazy val transformacion: Parser[Figura] = color <|> escala <|> rotacion <|> traslacion

    private lazy val figura: Parser[Figura] = figuraSimple <|> transformacion <|> grupo

    private val espacios: Parser[List[Char]] = (char(' ') <|> char('\r') <|> char('\n') <|> char('\t')).*

    private val rgb: Parser[Int] = integer.satisfies(n => n >= 0 && n <= 255)

    private def normalizarGrados(grados: Double) = ((grados % 360) + 360) % 360

    private def argumento[T](parser: Parser[T]) = for {
      _ <- espacios
      p <- parser
      _ <- espacios
    } yield p

    private def argumentos[T](tipo: Parser[T], principio: Char, fin: Char): Parser[List[T]] = for {
      _ <- char(principio)
      p <- argumento(tipo).sepBy(char(','))
      _ <- char(fin)
    } yield p

    private def argumentos[T](tipo: Parser[T], principio: Char, fin: Char, cantidad: Int): Parser[List[T]] =
      argumentos(tipo, principio, fin).satisfies(_.size == cantidad)

    def parsearFigura(entrada: String): Try[Figura] = {
      print("se va a parsear la siguiente figura: " + figura.funcionParser(entrada))
      Try(simplificadorFiguras.simplificar(figura.funcionParser(entrada).map(_._1).get))
    }
    val punto: Parser[coordenada] = for {
      x <- integer
      _ <- espacios
      _ <- char('@')
      _ <- espacios
      y <- integer
    } yield (x, y)

    val triangulo: Parser[Triangulo] = for {
      _ <- string("triangulo")
      puntos <- argumentos(punto, '[', ']', 3)
    } yield Triangulo(puntos.head, puntos(1), puntos(2))

    val rectangulo: Parser[Rectangulo] = for {
      _ <- string("rectangulo")
      puntos <- argumentos(punto, '[', ']', 2)
    } yield Rectangulo(puntos.head, puntos(1))

    val circulo: Parser[Circulo] = for {
      _ <- string("circulo")
      _ <- char('[')
      centro <- argumento(punto)
      _ <- char(',')
      radio <- argumento(integer)
      _ <- char(']')
    } yield Circulo(centro, radio)

    val grupo: Parser[Grupo] = for {
      _ <- string("grupo")
      figuras <- argumentos(figura, '(', ')')
    } yield Grupo(figuras)

    private val figuraTransformada = argumentos(figura, '(', ')', 1).map(_.head)

    val color: Parser[ColorFigura] = for {
      _ <- string("color")
      valores <- argumentos(rgb, '[', ']', 3)
      figura <- figuraTransformada
    } yield ColorFigura((valores.head, valores(1), valores(2)), figura)

    val escala: Parser[Escala] = for {
      _ <- string("escala")
      factores <- argumentos(double, '[', ']', 2)
      figura <- figuraTransformada
    } yield Escala(factores.head, factores(1), figura)

    val rotacion: Parser[Rotacion] = for {
      _ <- string("rotacion")
      grados <- argumentos(double, '[', ']', 1).map(_.head)
      figura <- figuraTransformada
    } yield Rotacion(normalizarGrados(grados), figura)

    val traslacion: Parser[Traslacion] = for {
      _ <- string("traslacion")
      d <- argumentos(integer, '[', ']', 2)
      figura <- figuraTransformada
    } yield Traslacion(d.head, d(1), figura)
  }

  object simplificadorFiguras {
    def simplificar(figura: Figura): Figura = figura match {
      case t: Metamorfosis => simplificarTransformacion(t)
      case g: Grupo => simplificarGrupo(g)
      case f => f
    }

    private def simplificarTransformacion(transformacion: Metamorfosis): Figura = {
      val figuraTransformadaSimplificada = simplificar(transformacion.getFiguraInterna)

      (transformacion, figuraTransformadaSimplificada) match {
        case (t, _) if t.esNula => figuraTransformadaSimplificada
        case (r1: Rotacion, r2: Rotacion) => r1.combinar(r2)
        case (e1: Escala, e2: Escala) => e1.combinar(e2)
        case (t1: Traslacion, t2: Traslacion) => t1.combinar(t2)
        case (c1: ColorFigura, c2: ColorFigura) => c1.combinar(c2)
        case _ => transformacion.aplicarA(figuraTransformadaSimplificada)
      }
    }

    private def simplificarGrupo(grupo: Grupo): Figura = {
      val figurasSimplificadas = grupo.figuras.map(simplificar)

      figurasSimplificadas match {
        case (t: Metamorfosis) :: figuras if t.esIgualA(figuras) =>
          t.aplicarA(Grupo(figurasSimplificadas.map(_.getFiguraInterna)))
        case _ => Grupo(figurasSimplificadas)
      }
    }
  }

}

object main extends App {

  import ParserImagenes._

  TADPDrawingAdapter.forScreen(adapter =>
    //val triangulo = parserFigurasRenovado.parsearFigura("traslacion[200, 50](\n\ttriangulo[0 @ 100, 200 @ 300, 150 @ 500]\n)").get.print(adapter)
    //val gotita = parserFigurasRenovado.parsearFigura("color[60, 150, 200](\n    grupo(\n   \t triangulo[200 @ 50, 101 @ 335, 299 @ 335],\n   \t circulo[200 @ 350, 100]\n    )\n)").get.print(adapter)
    //val murcielago = parserFigurasRenovado.parsearFigura("grupo(escala[1.2, 1.2](grupo(color[0, 0, 80](rectangulo[0 @ 0, 600 @ 700]), color[255, 255, 120](circulo[80 @ 80, 50]), color[0, 0, 80](circulo[95 @ 80, 40]))), color[50, 50, 50](triangulo[80 @ 270, 520 @ 270, 300 @ 690]), color[80, 80, 80](triangulo[80 @ 270, 170 @ 270, 300 @ 690]), color[100, 100, 100](triangulo[200 @ 200, 400 @ 200, 300 @ 150]), color[100, 100, 100](triangulo[200 @ 200, 400 @ 200, 300 @ 400]), color[150, 150, 150](triangulo[400 @ 200, 300 @ 400, 420 @ 320]), color[150, 150, 150](triangulo[300 @ 400, 200 @ 200, 180 @ 320]), color[100, 100, 100](triangulo[150 @ 280, 200 @ 200, 180 @ 320]), color[100, 100, 100](triangulo[150 @ 280, 200 @ 200, 150 @ 120]), color[100, 100, 100](triangulo[400 @ 200, 450 @ 300, 420 @ 320]), color[100, 100, 100](triangulo[400 @ 200, 450 @ 300, 450 @ 120]), grupo(escala[0.4, 1](color[0, 0, 0](grupo(circulo[970 @ 270, 25], circulo[530 @ 270, 25])))))").get.print(adapter)
    //val Composicion3 = parserFigurasRenovado.parsearFigura("escala[1.45, 1.45](grupo(color[0, 0, 0](rectangulo[0 @ 0, 400 @ 400]), color[200, 70, 0](rectangulo[0 @ 0, 180 @ 150]), color[250, 250, 250](grupo(rectangulo[186 @ 0, 400 @ 150], rectangulo[186 @ 159, 400 @ 240], rectangulo[0 @ 159, 180 @ 240], rectangulo[45 @ 248, 180 @ 400], rectangulo[310 @ 248, 400 @ 400], rectangulo[186 @ 385, 305 @ 400])), color[30, 50, 130](rectangulo[186 @ 248, 305 @ 380]), color[250, 230, 0](rectangulo[0 @ 248, 40 @ 400])))").get.print(adapter)
    //val red = parserFigurasRenovado.parsearFigura("color[100, 100, 100](\n  grupo(\n    color[0, 0, 0](\n      grupo(\n        color[201, 176, 55](\n          triangulo[0 @ 0, 650 @ 0, 0 @ 750]\n        ),\n        color[215, 215, 215](\n          triangulo[650 @ 750, 650 @ 0, 0 @ 750]\n        ),\n        color[255, 255, 255](\n          grupo(\n            rectangulo[230 @ 150, 350 @ 180],\n            rectangulo[110 @ 150, 470 @ 390]\n          )\n        ),\n        color[255, 0, 0](\n          grupo(\n            rectangulo[170 @ 60, 410 @ 150],\n            rectangulo[350 @ 60, 380 @ 180],\n            rectangulo[200 @ 60, 230 @ 180],\n            rectangulo[260 @ 300, 320 @ 330],\n            rectangulo[170 @ 390, 410 @ 480]\n          )\n        ),\n        rectangulo[200 @ 180, 380 @ 210],\n        rectangulo[230 @ 240, 260 @ 300],\n        rectangulo[320 @ 240, 350 @ 300],\n        rectangulo[200 @ 30, 380 @ 60],\n        rectangulo[170 @ 60, 200 @ 90],\n        rectangulo[380 @ 60, 410 @ 90],\n        rectangulo[140 @ 90, 170 @ 150],\n        rectangulo[410 @ 90, 440 @ 150],\n        rectangulo[110 @ 150, 200 @ 180],\n        rectangulo[110 @ 180, 170 @ 210],\n        rectangulo[140 @ 210, 170 @ 240],\n        rectangulo[80 @ 210, 110 @ 270],\n        rectangulo[110 @ 270, 170 @ 330],\n        rectangulo[110 @ 300, 200 @ 330],\n        rectangulo[80 @ 330, 110 @ 390],\n        rectangulo[110 @ 390, 200 @ 420],\n        rectangulo[140 @ 420, 170 @ 480],\n        rectangulo[200 @ 420, 260 @ 450],\n        rectangulo[320 @ 420, 380 @ 450],\n        rectangulo[260 @ 390, 320 @ 420],\n        rectangulo[170 @ 330, 410 @ 390],\n        rectangulo[170 @ 480, 260 @ 510],\n        rectangulo[260 @ 450, 320 @ 480],\n        rectangulo[320 @ 480, 410 @ 510],\n        rectangulo[410 @ 420, 440 @ 480],\n        rectangulo[380 @ 390, 470 @ 420],\n        rectangulo[470 @ 330, 500 @ 390],\n        rectangulo[380 @ 300, 470 @ 330],\n        rectangulo[410 @ 270, 470 @ 330],\n        rectangulo[470 @ 210, 500 @ 270],\n        rectangulo[410 @ 210, 440 @ 240],\n        rectangulo[410 @ 180, 470 @ 210],\n        rectangulo[380 @ 150, 470 @ 180],\n        rectangulo[380 @ 150, 470 @ 180]\n      )\n    )\n  )\n)").get.print(adapter)
    val pepita = parserFigurasRenovado.parsearFigura("grupo(\n\tcolor[0, 0, 80](\n\t\tgrupo(\n\t\t\ttriangulo[50 @ 400, 250 @ 400, 200 @ 420],\n\t\t\ttriangulo[50 @ 440, 250 @ 440, 200 @ 420]\n\t\t)\n\t),\n\tcolor[150, 150, 150](\n\t\ttriangulo[200 @ 420, 250 @ 400, 250 @ 440]\n\t),\n\tcolor[180, 180, 160](\n\t\ttriangulo[330 @ 460, 250 @ 400, 250 @ 440]\n\t),\n\tcolor[200, 200, 180](\n\t\tgrupo(\n\t\t\ttriangulo[330 @ 460, 400 @ 400, 330 @ 370],\n\t\t\ttriangulo[330 @ 460, 400 @ 400, 370 @ 450],\n\t\t\ttriangulo[400 @ 430, 400 @ 400, 370 @ 450],\n\t\t\ttriangulo[330 @ 460, 250 @ 400, 330 @ 370]\n\t\t)\n\t),\n\tgrupo(\n\t\tcolor[150, 0, 0](\n\t\t\tgrupo(\n\t\t\t\ttriangulo[430 @ 420, 400 @ 400, 450 @ 400],\n\t\t\t\ttriangulo[430 @ 420, 400 @ 400, 400 @ 430]\n\t\t\t)\n\t\t),\n\t\tcolor[100, 0, 0](triangulo[420 @ 420, 420 @ 400, 400 @ 430]),\n\t\tcolor[0, 0, 60](\n\t\t\tgrupo(\n\t\t\t\ttriangulo[420 @ 400, 400 @ 400, 400 @ 430],\n\t\t\t\ttriangulo[420 @ 380, 400 @ 400, 450 @ 400],\n\t\t\t\ttriangulo[420 @ 380, 400 @ 400, 300 @ 350]\n\t\t\t)\n\t\t),\n\t\tcolor[150, 150, 0](triangulo[440 @ 410, 440 @ 400, 460 @ 400])\n\t),\n\tcolor[0, 0, 60](\n\t\tgrupo(\n\t\t\ttriangulo[330 @ 300, 250 @ 400, 330 @ 370],\n\t\t\ttriangulo[330 @ 300, 400 @ 400, 330 @ 370],\n\t\t\ttriangulo[360 @ 280, 400 @ 400, 330 @ 370],\n\t\t\ttriangulo[270 @ 240, 100 @ 220, 330 @ 370],\n\t\t\ttriangulo[270 @ 240, 360 @ 280, 330 @ 370]\n\t\t)\n\t)\n)").get.print(adapter)

  )
}


