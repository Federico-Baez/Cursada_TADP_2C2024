require_relative './mixin_objeto_espiado.rb'
module TestSuiteMixin
  def ser(unObjeto)
    # Si lo que paso por parametro es un proc, pasa de largo. Si no, "ser" actua como la igualdad
    if unObjeto.class == Proc
      unObjeto
    else
      proc do |otroObjeto|
        if unObjeto == otroObjeto
          unObjeto == otroObjeto
        else
          raise ErrorTestFallido.new("Se esperaba #{unObjeto} y se encontro #{otroObjeto}")
        end
      end
    end
  end

  def ser_igual(unObjeto)
    ser(unObjeto)
  end

  def mayor_a(unObj)
    proc do |otroObj|
      if otroObj > unObj
        otroObj > unObj
      else
        raise ErrorTestFallido.new("Se esperaba un numero mayor a #{unObj} y se encontro #{otroObj}")
      end
    end
  end

  def menor_a(unObj)
    proc do |otroObj|
      if otroObj <= unObj
        otroObj <= unObj
      else
        raise ErrorTestFallido.new("Se esperaba un numero menor a #{unObj} y se encontro #{otroObj}")
      end
    end
  end

  def uno_de_estos(*unArray)
    # Si lo que paso es un array, al agregar el Splat operator en uno_de_estos(*unArray) va a convertirse en un arreglo de arr
    if unArray[0].class == Array
      proc do |otroObj|
        if unArray[0].include?(otroObj)
           unArray[0].include?(otroObj)
        else
          raise ErrorTestFallido.new("Se esperaba un objeto entre uno de #{unArray.to_s} y se encontro #{otroObj}")
        end
      end
    else
      proc do |otroObj|
        if unArray.include?(otroObj)
          unArray.include?(otroObj)
        else
          raise ErrorTestFallido.new("Se esperaba un objeto entre uno de #{unArray.to_s} y se encontro #{otroObj}")
        end
      end
    end
  end

  def entender(unSimbolo)
    proc do |unObj|
      if unObj.respond_to?(unSimbolo)
         unObj.respond_to?(unSimbolo)
      else
        raise ErrorTestFallido.new("Se esperaba que #{unObj} entienda el mensaje #{unSimbolo} y se encontro  que no lo entiende")
      end
    end
  end

  def explotar_con(tipoDeError)
    proc { |operacionQueRompe|
      begin
        operacionQueRompe.call # Ejecuta el proc que podría fallar
      rescue => errorDeOperacionQueRompe
        # Compara si el tipo de error capturado es del tipo esperado
        if errorDeOperacionQueRompe.is_a? tipoDeError
          errorDeOperacionQueRompe.is_a? tipoDeError
        else
            raise ErrorTestFallido.new("Se esperaba un tipo de error #{tipoDeError} se genero un error #{errorDeOperacionQueRompe}")
        end
      end
    }
  end

  def haber_recibido(simbolo)
    miProc = proc do |unObjeto|
      if TADsPec.seUtilizoElMetodo?(unObjeto, simbolo)
        TADsPec.seUtilizoElMetodo?(unObjeto, simbolo)
      else
        raise ErrorTestFallido.new("Se esperaba que #{unObjeto} hubiera utilizado #{simbolo} y no lo hizo")
      end
    end
    miProc.simbolo = simbolo
    miProc
  end

  def espiar(unObjeto)
    (unObjeto.public_methods - Object.methods).each { |metodo|
      original_method = unObjeto.method(metodo)
      TADsPec.salvarImplmentacionDeMetodosDeObjetosEspiados(unObjeto, metodo, original_method)
      unObjeto.define_singleton_method(metodo) do |*args, &block|
        # Lógica nueva antes de la ejecución del método original
        TADsPec.registrarMetodoDeObjetoEspiado(self, metodo, args)
        # Ejecutar la lógica original
        original_method.call(*args, &block)
      end
    }
    unObjeto.extend MixinObjetoEspiado
    unObjeto
    # return el objeto espiado
  end

end




