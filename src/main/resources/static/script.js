// O início do arquivo (variáveis globais e lógica do autocomplete) permanece o mesmo.
const form = document.getElementById('simulador-form');
const tickerInput = document.getElementById('ticker');
const valorInput = document.getElementById('valor');
const mesesInput = document.getElementById('meses');
const resultadoDiv = document.getElementById('resultado');
const sugestoesDiv = document.getElementById('sugestoes');
let todasAsAcoes = [];

// ... (toda a lógica do autocomplete, de carregarAcoesDisponiveis até o final, continua exatamente a mesma)
async function carregarAcoesDisponiveis() {
    try {
        const response = await fetch('/api/investimentos/acoes-disponiveis');
        if (!response.ok) throw new Error('Falha ao carregar lista de ações.');
        const data = await response.json();
        todasAsAcoes = data.stocks;
    } catch (error) {
        console.error("Erro ao carregar ações:", error);
    }
}
tickerInput.addEventListener('input', () => {
    const textoDigitado = tickerInput.value.toUpperCase();
    sugestoesDiv.innerHTML = '';
    if (textoDigitado.length === 0) {
        sugestoesDiv.classList.add('hidden');
        return;
    }
    const acoesFiltradas = todasAsAcoes.filter(acao => acao.startsWith(textoDigitado)).slice(0, 7);
    if (acoesFiltradas.length > 0) {
        sugestoesDiv.classList.remove('hidden');
        acoesFiltradas.forEach(acao => {
            const item = document.createElement('div');
            item.className = 'sugestao-item';
            item.textContent = acao;
            item.addEventListener('click', () => {
                tickerInput.value = acao;
                sugestoesDiv.classList.add('hidden');
            });
            sugestoesDiv.appendChild(item);
        });
    } else {
        sugestoesDiv.classList.add('hidden');
    }
});
document.addEventListener('click', function(event) {
    if (!tickerInput.contains(event.target)) {
        sugestoesDiv.classList.add('hidden');
    }
});

// A lógica da simulação também continua a mesma.
form.addEventListener('submit', async function(event) {
    event.preventDefault();
    const ticker = tickerInput.value.toUpperCase();
    const valorInvestido = parseFloat(valorInput.value);
    const quantidadeMeses = parseInt(mesesInput.value);
    resultadoDiv.innerHTML = '<div class="spinner"></div>';
    resultadoDiv.className = '';
    try {
        const requestBody = { valorInvestido, quantidadeMeses };
        const response = await fetch(`/api/investimentos/${ticker}/simular`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(requestBody)
        });
        if (!response.ok) throw new Error('Não foi possível encontrar a ação ou ocorreu um erro.');
        const data = await response.json();
        exibirResultado(data);
    } catch (error) {
        exibirErro(error.message);
    }
});

// ### A GRANDE MUDANÇA ESTÁ AQUI ###
function exibirResultado(data) {
    const formatarMoeda = (valor) => valor.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
    const formatarPercentual = (valor) => (valor * 100).toFixed(2).replace('.', ',') + '%';

    // Gera o HTML base para o resultado
    resultadoDiv.innerHTML = `
        <div class="resultado-item">
            <span>Ação</span>
            <strong>${data.longName} (${data.ticker})</strong>
        </div>
        <div class="resultado-item">
            <span>Rendimento Médio Mensal (3M)</span>
            <span id="rendimento-medio"></span>
        </div>
        <div class="resultado-item">
            <span>Valor Investido</span>
            <span>${formatarMoeda(data.valorInvestido)}</span>
        </div>
        <div class="resultado-item">
            <span>Período</span>
            <span>${data.quantidadeMeses} meses</span>
        </div>
        <div class="resultado-item">
            <span>Valor Final Bruto</span>
            <span>${formatarMoeda(data.valorFinalBruto)}</span>
        </div>
        <div class="resultado-item">
            <span>Imposto de Renda (15%)</span>
            <span>- ${formatarMoeda(data.impostoDeRenda)}</span>
        </div>
        <div class="resultado-item">
            <strong>Rendimento Líquido</strong>
            <span id="rendimento-liquido"></span>
        </div>
    `;

    // Pega os elementos que precisam de cor
    const rendimentoMedioEl = document.getElementById('rendimento-medio');
    const rendimentoLiquidoEl = document.getElementById('rendimento-liquido');

    // Preenche e colore o Rendimento Médio
    rendimentoMedioEl.textContent = formatarPercentual(data.rendimentoMedioMensalCalculado);
    rendimentoMedioEl.className = data.rendimentoMedioMensalCalculado >= 0 ? 'positivo' : 'negativo';

    // Preenche e colore o Rendimento Líquido
    rendimentoLiquidoEl.textContent = formatarMoeda(data.rendimentoLiquido);
    rendimentoLiquidoEl.className = data.rendimentoLiquido >= 0 ? 'positivo' : 'negativo';
}

function exibirErro(mensagem) {
    resultadoDiv.className = 'error';
    resultadoDiv.innerHTML = `<p><strong>Erro:</strong> ${mensagem}</p>`;
}

carregarAcoesDisponiveis();