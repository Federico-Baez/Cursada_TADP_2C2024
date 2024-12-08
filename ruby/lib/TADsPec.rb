require_relative './OperadorDinamico.rb'
require_relative './deberia.rb'
require_relative './manera_de_mokear.rb'
require_relative './mixin_object.rb'
require_relative './mixin_procs.rb'

# Creamos un módulo que actuará como framework de pruebas
module TADsPec
  # Lista de todas las suites registradas
  @test_suites = []
  @metodosMokeados = {}
  @metodosEspiados = {}
  @objetosEspiados = {}
  @cantidadDeTestCorridos = 0
  @cantidadDeTestPasadosExito = 0
  @cantidadDeTestFallados = 0
  @cantidadTestExplotados = 0
  @listaDeTestFallados = []
  @listaTestExplotados = []
  @listaTestPasadosConExito = []

  def self.sumarCantidadDeTestCorridos
    @cantidadDeTestCorridos += 1
  end

  def self.sumarCantidadDeTestPasadosConExito
    @cantidadDeTestPasadosExito += 1
  end

  def self.sumarCantidadTestFallados
    @cantidadDeTestFallados += 1
  end

  def self.sumarCantidadTestExplotados
    @cantidadTestExplotados += 1
  end

  def self.agregarTestFallado(testFallado)
    @listaDeTestFallados << testFallado
  end

  def self.agregarTestPasado(testPasado)
    @listaTestPasadosConExito << testPasado
  end

  def self.agregarTestExplotado(testExplotado)
    @listaTestExplotados << testExplotado
  end

  def self.registrarMetodoMokeado(objeto, metodo, metodoOriginal)
    @metodosMokeados[[objeto, metodo]] = metodoOriginal
  end

  def self.registrarMetodoDeObjetoEspiado(objetoEspiado, metodo, args)
    @metodosEspiados[objetoEspiado] ||= []
    @metodosEspiados[objetoEspiado].push [metodo, args]
  end

  def self.seUtilizoConArgumentos(objeto, simbolo, parametros)
    @metodosEspiados[objeto].any? { |llamado| llamado[0] == simbolo && llamado[1] == parametros }
  end

  def self.seUtilizoElMetodo?(objeto, simbolo)
    if objeto.is_a? MixinObjetoEspiado
      @metodosEspiados[objeto].any? { |llamado| llamado[0] == simbolo }
    end
  end

  def self.cuantoSeUtilizoEsteMetodo(objetoEspiado, unMetodo)
    @metodosEspiados[objetoEspiado].count { |llamado| llamado[0] == unMetodo }
  end

  def self.salvarImplmentacionDeMetodosDeObjetosEspiados(objeto, nombreMetodo, metodo)
    @objetosEspiados[[objeto, nombreMetodo]] = metodo
  end

  def self.limpiarVariables
    @metodosMokeados = {}
    @metodosEspiados = {}
    @objetosEspiados = {}
    @cantidadDeTestCorridos = 0
    @cantidadDeTestPasadosExito = 0
    @cantidadDeTestFallados = 0
    @cantidadTestExplotados = 0
    @listaDeTestFallados = []
    @listaTestExplotados = []
    @listaTestPasadosConExito = []
  end

  # Método para registrar las suites de test
  def self.registrar_suite(test_suite)
    unless @test_suites.include?(test_suite)
      @test_suites << test_suite
    end
  end

  # Método para ejecutar los tests
  def self.testear(*args)
    Object.include(Deberia)
    Object.subclasses.each { |subclass|
      if subclass.instance_methods.any? { |m| m.to_s.start_with?("testear_que_") }
        subclass.include(TestSuiteMixin)
        registrar_suite(subclass)
      end
    }
    if args.empty?
      # Si no se pasan argumentos, se ejecutan todos los tests de todas las suites registradas
      @test_suites.each do |suite|
        correr_tests_suite(suite)
      end
    else
      suite = args[0]
      if args.length == 1
        # Si solo se pasa una suite, se ejecutan todos los tests de esa suite
        correr_tests_suite(suite)
      else
        # Se ejecutan tests específicos de la suite
        tests_a_correr = args[1..-1]
        correr_tests_especificos(suite, tests_a_correr)
      end
    end
  end

  # Método para ejecutar todos los tests de una suite
  def self.correr_tests_suite(suite)
    puts "\nCorriendo todos los tests de la suite #{suite}..."
    suite.instance_methods.grep(/^testear_que_/).each do |test|
      correr_test(suite,test)
    end

    puts "Cantidad total de test corridos: #{@cantidadDeTestCorridos}"
    puts "Cantidad total de test pasados con exito: #{@cantidadDeTestPasadosExito}"
    puts "Cantidad total de test fallados: #{@cantidadDeTestFallados}"
    puts "Cantidad total de test explotados: #{@cantidadTestExplotados}"

    puts "Los test que pasaron con exito fueron: #{@listaTestPasadosConExito.to_s}"
    puts "Los test que fallaron fueron: #{@listaDeTestFallados.to_s}"
    puts "Los test que explotaron fueron: #{@listaTestExplotados.to_s}"
    limpiarVariables
  end

  # Método para ejecutar tests específicos de una suite
  def self.correr_tests_especificos(suite, tests)
    puts " \ nCorriendo tests específicos de la suite #{suite}: #{tests.join(', ')}..."
    tests.each do | test |
      if suite.instance_methods.include?(test)
        correr_test(suite, test)
      else
        puts "  .Test ##{test} no encontrado en la suite #{suite}."
      end
    end
    end

    def self.restaurarMetodosMokeados
      @metodosMokeados.each do |(clase, nombreMetodo), metodoOriginal|
        clase.define_method(nombreMetodo, metodoOriginal)
      end
    end

    # Método para ejecutar un test específico
    def self.restaurarObjetosEspiados
      @objetosEspiados.each do |(objeto, nombreMetodo), metodo|
        objeto.define_singleton_method(nombreMetodo) do |*args, &block|
          metodo.call(*args, &block)
        end
      end
    end

    def self.correr_test(suite, test)
      puts "  .Corriendo ##{test}..."
      Object.include(ManeraDeMokear)
      Object.include(MixinObject)
      Proc.include(MixinProcs)
      sumarCantidadDeTestCorridos

      suite.new.send(test)
      restaurarMetodosMokeados
      restaurarObjetosEspiados
      # Restauro los metodos de todos los test mokeados
    rescue => e
      if e.is_a? ErrorTestFallido
        agregarTestFallado("El test #{test} fallo por #{ErrorTestFallido.to_s}")
        sumarCantidadTestFallados
      else
        agregarTestExplotado("El test #{test} exploto por #{ErrorTestFallido.to_s}")
        sumarCantidadTestExplotados
      end
      puts "    ->FAIL: #{e.message}"
    else
      agregarTestPasado("#{test}")
      sumarCantidadDeTestPasadosConExito
      puts "    ->PASS: Test pasó con éxito."
    end

    end



