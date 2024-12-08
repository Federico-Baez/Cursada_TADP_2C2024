require_relative './TestSuiteMixin'

class OperadorDinamico
  attr_accessor :operacion
  
  def initialize(metodo, args)
      if(args.length > 0) # caso 'tener_[metodo] [param]'
        @operacion = proc { |unObjeto| unObjeto.send(metodo) == args[0]}
      elsif(args.length > 0 && args[0].class == Proc ) # caso 'tener_[metodo] [proc]'
        @operacion = proc { |unObjeto|  args[0].call(unObjeto.send(metodo))}
      else
        @operacion = proc { |unObjeto| unObjeto.send(metodo)}
      end
  end

  def call(llamador) # Defino call para hacer polimorfismo con los Procs
    @operacion.call(llamador)
  end
end
