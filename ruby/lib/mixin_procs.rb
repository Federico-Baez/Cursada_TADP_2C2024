module MixinProcs
  attr_accessor :simbolo

  def veces(cantidad)
    proc do  |unObjeto|
      if cantidad == TADsPec.cuantoSeUtilizoEsteMetodo(unObjeto, self.simbolo)
       cantidad == TADsPec.cuantoSeUtilizoEsteMetodo(unObjeto, self.simbolo)
      else
        raise ErrorTestFallido.new("Se esperaba que #{unObjeto} hubiera utilizado #{self.simbolo} #{cantidad} veces")
      end
    end
  end

  def con_argumentos(*args)
    proc do |unObjeto|
      if TADsPec.seUtilizoConArgumentos(unObjeto, self.simbolo, args)
         TADsPec.seUtilizoConArgumentos(unObjeto, self.simbolo, args)
      else
        raise ErrorTestFallido.new("Se esperaba que #{unObjeto} hubiera utilizado #{self.simbolo} con los argumentos #{args}")
      end
    end
  end
end
