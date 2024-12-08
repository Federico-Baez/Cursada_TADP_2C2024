require_relative './spec_helper.rb'
tadp = Materia.new
persona_vieja = Persona.new 40
persona_joven = Persona.new 23
nico = Persona.new 33,tadp
erwin = Persona.new 29,tadp
leandro = Persona.new 25

#-------------------- SER -----------------------------

RSpec.describe 'Deberia ser' do
  it 'tadp.docentes[0].deberia ser nico => true' do
    expect(tadp.docentes[0].deberia ser nico).to eq(true)
  end
  it '7.deberia ser 7 => true' do
    expect(7.deberia ser 7).to eq(true)
  end
  it 'true.deberia ser false => false' do
    expect(true.deberia ser false).to eq(false)
  end
  it 'leandro.edad.deberia ser 25 => true' do
    expect(leandro.edad.deberia ser 25).to eq(true)
  end
  it 'Una persona deberia ser si misma' do
    expect(persona_joven.deberia ser persona_joven).to eq(true)
  end
end

#-------------------- SER_IGUAL -----------------------------

RSpec.describe 'Deberia ser_igual' do
  it 'erwin.docente.deberia ser_igual => true' do
    expect(erwin.docente.deberia ser_igual true).to eq(true)
  end
  it 'erwin.edad.deberia ser_igual  18 => false' do
    expect(erwin.edad.deberia ser_igual  18).to eq(false)
  end
  it 'erwin.deberia ser_igual  erwin => true' do
    expect(erwin.deberia ser_igual  erwin).to eq(true)
  end
end

#-------------------- MENOR A -----------------------------

RSpec.describe 'Deberia ser menor_a' do
  it 'erwin.edad.deberia ser menor_a  40 => true' do
    expect(erwin.edad.deberia ser menor_a  40).to eq(true)
  end
  it 'leandro.edad.deberia ser menor_a 25 => true' do
    expect(leandro.edad.deberia ser menor_a 25).to eq(true)
  end
end

#-------------------- MAYOR A -----------------------------

RSpec.describe 'Deberia ser mayor_a' do
  it 'leandro.edad.deberia ser mayor_a 20 => true' do
    expect(leandro.edad.deberia ser mayor_a 20).to eq(true)
  end
end

# ------------------------------------- PRUEBAS DE TENER_ --------------------------

#RSPEC NO LO ENTIENDE
#RSpec.describe 'Azucar Sintáctico' do
  #RSPEC NO LO ENTIENDE
  # it 'nico.deberia ser_viejo => true' do
  #   expect(nico.deberia ser_viejo).to eq(true)
  # end

  # it 'nico.viejo?.deberia ser true => true' do
  #  expect(nico.viejo?.deberia ser true).to eq(true)
  # end

  # it 'leandro.deberia ser_viejo => false' do
  #   expect(leandro.deberia ser_viejo).to eq(false)
  # end
  #
  # it 'Una persona vieja deberia ser_viejo => true' do
  #   expect(persona_vieja.deberia ser_viejo).to eq(true)
  # end
#end
#-------------------- ENTENDER -----------------------------

RSpec.describe 'Entender' do
  it 'Una persona entiende el mensaje edad => true' do
    expect(persona_vieja.deberia entender :edad).to eq(true)
  end
  it 'Una persona no deberia entender un mensaje cualquiera' do
    expect(persona_vieja.deberia entender :sarasa).to eq(false)
  end

  it 'leandro.deberia entender :viejo? => True' do
    expect(leandro.deberia entender :viejo?).to eq(true)
  end
  it 'leandro.deberia entender :class => True' do
    expect(leandro.deberia entender :class).to eq(true)
  end
  it 'leandro.deberia entender :nombre => false' do
    expect(leandro.deberia entender :nombre).to eq(false)
  end

  it 'leandro.deberia entender :method_missing => true' do
    expect(leandro.deberia entender :method_missing).to eq(true)
  end

end



