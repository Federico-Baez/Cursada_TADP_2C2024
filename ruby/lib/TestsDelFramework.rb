require_relative './TADsPec.rb'

# ---------------------- DEFINICIONES --------------------------
# Before definitions
class Materia
  attr_accessor :docentes

  def initialize()
    self.docentes = []
  end

  def add_docente(docente)
    self.docentes << docente
  end
end

class Persona
  attr_reader :edad, :materia, :docente

  def initialize(edad, materia = nil)
    @edad = edad
    if (materia)
      @docente = true
      materia.add_docente(self)
    end
  end

  def viejo?
    self.edad > 25
  end

  def hablar(frase)
    puts frase
  end
end

class Comida
  attr_accessor :nombre, :calorias

  def initialize(nombre, calorias = 0)
    @nombre = nombre
    @calorias = calorias
  end
end

# ---------------------- PRUEBAS DE AZUCAR SINTACTICO --------------------------
tadp = Materia.new
persona_vieja = Persona.new 40
persona_joven = Persona.new 23
nico = Persona.new 33, tadp
erwin = Persona.new 29, tadp
leandro = Persona.new 22

# #################################################################################
# -------------- PRUEBAS A SER EJECUTADAS A TRAVES DE LA CONSOLA-----------------
# #################################################################################

####################################### Pruebas Deberia Ser ###########################################
class PruebasTestSuitDeberiaSer
  attr_accessor :leandro

  def initialize
    @leandro = Persona.new(22) # Inicializamos correctamente a "leandro" en el constructor
  end

  def testear_que_7_deberia_ser_7
    7.deberia ser 7 # pasa
  end

  def testear_que_2_deberia_ser_3_falla
    2.deberia ser 3 # falla
  end

  def testear_que_true_deberia_ser_false_falla
    true.deberia ser false # falla
  end

  def testear_que_leandro_edad_deberia_ser_25_falla
    leandro.edad.deberia ser 25 # falla
  end

  def testear_que_7_deberia_ser_igual_7
    7.deberia ser_igual 7 # pasa
  end

  def testear_que_2_deberia_ser_igual_3_falla
    2.deberia ser_igual 3 # falla
  end
end

####################################### Pruebas Deberia Ser Variantes Mayor / Menor ###########################################
class PruebasTestSuitDeberiaSerMayorMenor
  attr_accessor :leandro

  def initialize
    @leandro = Persona.new(22) # Inicializamos correctamente a "leandro" en el constructor
  end

  def testear_que_7_deberia_ser_mayor_que_3
    7.deberia ser mayor_a 3 # pasa
  end

  def testear_que_3_deberia_ser_mayor_que_5_falla
    4.deberia ser mayor_a 5 # falla
  end

  def testear_que_2_deberia_ser_menor_que_6
    2.deberia ser menor_a 6 # pasa
  end

  def testear_que_9_deberia_ser_menor_que_4_falla
    9.deberia ser menor_a 4 # falla
  end
end

####################################### Pruebas Deberia Ser Variante Uno De Estos ###########################################
class PruebasTestSuitDeberiaSerUnoDeEstos
  attr_accessor :leandro

  def initialize
    @leandro = Persona.new(22) # Inicializamos correctamente a "leandro" en el constructor
  end

  def testear_que_leandro_edad_deberia_ser_uno_de_estos_array
    leandro.edad.deberia ser uno_de_estos [7, 22, "hola"] # pasa
  end

  def testear_que_leandro_edad_deberia_ser_uno_de_estos_varArgs
    leandro.edad.deberia ser uno_de_estos 7, 22, "hola" # pasa
  end

  def testear_que_leandro_edad_deberia_ser_uno_de_estos_array_falla
    leandro.edad.deberia ser uno_de_estos [7, 32, "hola"] # falla
  end
end

####################################### Pruebas Deberia Tener ###########################################
class PruebasTestSuitDeberiaTener
  attr_accessor :leandro

  def initialize
    @leandro = Persona.new(22) # Inicializamos correctamente a "leandro" en el constructor
  end

  def testear_que_leandro_deberia_tener_edad_22
    leandro.deberia tener_edad 22 # pasa
  end

  def testear_que_leandro_deberia_tener_nombre_leandro_falla
    leandro.deberia tener_nombre "leandro" # falla
  end
end

####################################### Pruebas Deberia Entender ###########################################
class PruebasTestSuitDeberiaEntender
  attr_accessor :leandro

  def initialize
    @leandro = Persona.new(22) # Inicializamos correctamente a "leandro" en el constructor
  end

  def testear_que_leandro_deberia_entender_viejo?
    leandro.deberia entender :viejo? # pasa
  end

  def testear_que_leandro_deberia_entender_class
    leandro.deberia entender :class # pasa
  end

  def testear_que_leandro_deberia_entender_nombre_falla
    leandro.deberia entender :nombre # falla
  end

end

####################################### Pruebas Deberia Explotar_con ###########################################
class PruebasTestSuitDeberiaExplotar
  attr_accessor :leandro

  def initialize
    @leandro = Persona.new(22) # Inicializamos correctamente a "leandro" en el constructor
  end

  def testear_que_haya_division_por_cero_explote_con_ZeroDivisionError
    proc { 7 / 0 }.deberia explotar_con ZeroDivisionError # pasa
  end

  def testear_que_Leandro_nombre_explote_con_NoMethodError
    proc { leandro.nombre }.deberia explotar_con NoMethodError # pasa
  end

  def testear_que_haya_division_por_cero_explote_con_NoMethodError_falla
    proc { 7 / 0 }.deberia explotar_con NoMethodError # falla
  end

end

####################################### Pruebas Mockear ###########################################

class PruebasMockear
  def testear_que_una_persona_es_viaje_pero_enrrealidad_es_joven
    nico = Persona.new(2)

    # Mockeo el mensaje para no consumir el servicio y simplificar el test
    Persona.mockear(:viejo?) do
      true
    end

    esViejo = nico.viejo?

    esViejo.deberia ser true
  end

  def testear_que_una_pera_dice_que_es_una_manzana
    pera = Comida.new("pera", 100)
    Comida.mockear(:nombre) do
      "manzana"
    end
    pera.nombre.deberia ser "manzana"
  end

  def testear_que_un_arroz_con_pollo_no_es_una_manzana
    arrozConPollo = Comida.new("arrozConPollo", 1000)
    arrozConPollo.nombre.deberia ser "arrozConPollo"
  end

end

####################################### Pruebas Mockear ###########################################

class PruebasEspiar
  def testear_que_funciona_con_argumentos
    nico = Persona.new(2)

    nico = espiar(nico)
    nico.hablar("Hey que tal")

    nico.deberia haber_recibido(:hablar).con_argumentos("Hey que tal")
  end
end
