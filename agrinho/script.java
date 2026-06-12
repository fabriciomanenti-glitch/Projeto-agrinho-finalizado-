// Configurações básicas da Grade do Jogo
let tamanhoBloco = 25;
let tamanhoMapa = 15;
let mapa = [];
let tipoBlocoAtual = 'plantio';

// Posição inicial do cursor piscante (Centro do mapa)
let seletorX = 7;
let seletorZ = 7;

// Atributos de Status do Jogador
let producao = 0;
let ambiente = 100;
let dinheiro = 5000;
let ano = 1;
let estacao = "Primavera";
let contadorEstacao = 0;
let velocidadeTempo = 120; // Tempo em frames para mudar de estação

// Rotação e Distância da Câmera 3D
let anguloY = 0;
let anguloX = 0.6; 
let distanciaCamera = 450;

// Propriedades do Jogo para cada Bloco
const blocos = {
  terra: { cor: [139, 69, 19], nome: 'Terra 🌍', custo: 0, receita: 0, ambiente: 0, producao: 0 },
  plantio: { cor: [218, 165, 32], nome: 'Plantação de Grãos 🌾', custo: 100, receita: 300, ambiente: -4, producao: 12 },
  arvore: { cor: [46, 139, 87], nome: 'Árvore Nativa 🌳', custo: 50, receita: 20, ambiente: 12, producao: 1 },
  usina: { cor: [100, 100, 255], nome: 'Painel Solar ☀️', custo: 500, receita: 150, ambiente: 5, producao: 5 }
};

function setup() {
  // Cria o jogo ocupando a tela inteira de fundo
  createCanvas(windowWidth, windowHeight, WEBGL);
  
  // Popula o mapa inicial apenas com terra limpa
  for (let x = 0; x < tamanhoMapa; x++) {
    mapa[x] = [];
    for (let z = 0; z < tamanhoMapa; z++) {
      mapa[x][z] = 'terra';
    }
  }
  atualizarInterfaceHTML();
}

function draw() {
  background(35, 45, 55); // Cor de fundo "espaço/céu escuro"
  
  // Avanço automático do tempo (estações)
  contadorEstacao++;
  if (contadorEstacao >= velocidadeTempo) {
    contadorEstacao = 0;
    passarEstacao();
  }

  // Se arrastar o mouse, muda a órbita da câmera
  if (mouseIsPressed) {
    anguloY += (mouseX - pmouseX) * 0.01;
    anguloX += (mouseY - pmouseY) * 0.01;
    anguloX = constrain(anguloX, 0.2, HALF_PI - 0.1);
  }
  
  // Posicionamento da Câmera 3D usando trigonometria
  camera(
    sin(anguloY) * cos(anguloX) * distanciaCamera, 
    sin(anguloX) * distanciaCamera, 
    cos(anguloY) * cos(anguloX) * distanciaCamera, 
    0, 0, 0, 
    0, 1, 0
  );
  
  // Luzes do ambiente para criar sombras e profundidade
  ambientLight(140);
  directionalLight(255, 255, 255, 0.6, 1, -0.4);

  // Renderização da Grade de Blocos
  let offset = (tamanhoMapa * tamanhoBloco) / 2;
  for (let x = 0; x < tamanhoMapa; x++) {
    for (let z = 0; z < tamanhoMapa; z++) {
      let tipo = mapa[x][z];
      
      push();
      // Move para a posição correta na grade
      translate(x * tamanhoBloco - offset, 0, z * tamanhoBloco - offset);
      
      // Destaca o bloco se o seletor do teclado estiver em cima dele
      if (x === seletorX && z === seletorZ) {
        emissiveMaterial(100, 100, 0); // Brilho amarelado
      } else {
        emissiveMaterial(0);
      }
      
      fill(blocos[tipo].cor[0], blocos[tipo].cor[1], blocos[tipo].cor[2]);
      stroke(0, 40);
      
      // Dá alturas diferentes para diferenciar os prédios e árvores
      let altura = tamanhoBloco;
      if (tipo === 'arvore') altura = tamanhoBloco * 1.8;
      if (tipo === 'usina') altura = tamanhoBloco * 1.3;
      
      translate(0, -altura/2, 0); // Alinha os blocos no "chão"
      box(tamanhoBloco, altura, tamanhoBloco);
      pop();
    }
  }
}

// Passa o turno das estações e calcula lucros/perdas
function passarEstacao() {
  let estacoes = ["Primavera", "Verão", "Outono", "Inverno"];
  estacao = estacoes[(estacoes.indexOf(estacao) + 1) % 4];
  if (estacao === "Primavera") ano++;

  let lucroTurno = 0, mudancaAmbiente = 0, producaoTurno = 0;
  
  for (let x = 0; x < tamanhoMapa; x++) {
    for (let z = 0; z < tamanhoMapa; z++) {
      let b = blocos[mapa[x][z]];
      lucroTurno += b.receita;
      mudancaAmbiente += b.ambiente;
      producaoTurno += b.producao;
    }
  }

  dinheiro += lucroTurno;
  ambiente = constrain(ambiente + mudancaAmbiente, 0, 100);
  producao = producaoTurno;

  document.getElementById('mensagem-alerta').innerText = `Nova estação! Lucro: +$${lucroTurno} | Impacto Ambiental: ${mudancaAmbiente >= 0 ? '+' : ''}${mudancaAmbiente}`;
  atualizarInterfaceHTML();
}

// Envia os dados novos do JavaScript direto para as caixas do HTML/CSS
function atualizarInterfaceHTML() {
  document.getElementById('txt-ano').innerText = `Ano: ${ano} (${estacao})`;
  document.getElementById('txt-dinheiro').innerText = `Dinheiro: $${dinheiro}`;
  document.getElementById('txt-ambiente').innerText = `Ambiente: ${ambiente}%`;
  document.getElementById('txt-producao').innerText = `Produção: ${producao}`;
  document.getElementById('status-selecionado').innerText = `Selecionado: ${blocos[tipoBlocoAtual].nome} (Cursor em X:${seletorX}, Z:${seletorZ})`;
}

// Escuta os comandos do teclado
function keyPressed() {
  // Atalhos de seleção [1, 2, 3, 4]
  if (key === '1') tipoBlocoAtual = 'plantio';
  if (key === '2') tipoBlocoAtual = 'arvore';
  if (key === '3') tipoBlocoAtual = 'usina';
  if (key === '4') tipoBlocoAtual = 'terra';

  // Setas do teclado movem o seletor na grade
  if (keyCode === LEFT_ARROW)  seletorX = constrain(seletorX - 1, 0, tamanhoMapa - 1);
  if (keyCode === RIGHT_ARROW) seletorX = constrain(seletorX + 1, 0, tamanhoMapa - 1);
  if (keyCode === UP_ARROW)    seletorZ = constrain(seletorZ - 1, 0, tamanhoMapa - 1);
  if (keyCode === DOWN_ARROW)  seletorZ = constrain(seletorZ + 1, 0, tamanhoMapa - 1);

  // Barra de espaço constrói
  if (key === ' ') {
    let custo = blocos[tipoBlocoAtual].custo;
    if (dinheiro >= custo) {
      if (mapa[seletorX][seletorZ] !== tipoBlocoAtual) {
        dinheiro -= custo;
        mapa[seletorX][seletorZ] = tipoBlocoAtual;
        document.getElementById('mensagem-alerta').innerText = `Construiu ${blocos[tipoBlocoAtual].nome}!`;
      }
    } else {
      document.getElementById('mensagem-alerta').innerText = "Saldo de dinheiro insuficiente!";
    }
  }
  atualizarInterfaceHTML();
}

// Se você redimensionar a tela do navegador, o jogo se adapta
function windowResized() {
  resizeCanvas(windowWidth, windowHeight);
}