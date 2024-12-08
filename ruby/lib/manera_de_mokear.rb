module ManeraDeMokear
  def mockear(metodo,&bloqueCodigo)
    metodoOriginal = self.instance_method(metodo)

    TADsPec.registrarMetodoMokeado(self,metodo,metodoOriginal)

    #Mokeo el metodo
    self.define_method(metodo) do
      bloqueCodigo.call
    end
  end
end
