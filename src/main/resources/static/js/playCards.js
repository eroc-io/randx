//检查浏览器是否支持WebSocket
if (window.WebSocket) {
    console.log('This browser supports WebSocket');
} else {
    console.log('This browser does not supports WebSocket');
    alert('This browser does not supports WebSocket');
}
;

const CARD_INDEX = 7;
//Uint8Array，桌编号
var deckId = null;
//Uint8Array，盐信息
var salt = null;
//Uint8Array，服务器公钥
var dpk = null;
//string
var errMsg = null;
//Uint8Array，牌证明
var proof = [];
//Uint8Array
var cards = [];

var ws = new WebSocket("ws://localhost:8080/");
ws.binaryType = "arraybuffer";

ws.onopen = function (evt) {

    console.log("Connection open ...");

};

ws.onmessage = function (evt) {

    let buffer = evt.data;
    let [num, proBuffer] = subByte(buffer);

    switch (num) {
        case 0:
            let obj = readPbf("comms.proto", "OpenResponse", proBuffer);
            deckId = obj.deckId;
            console.log(deckId);
            console.log(obj.errMsg);
            break;
        case 1:
            let obj1 = readPbf("comms.proto", "JoinResponse", proBuffer);
            salt = obj1.salt;
            dpk = obj1.dpk;
            console.log(salt);
            console.log(dpk);
            console.log(obj1.errMsg);
            break;
        case 2:
            let obj2 = readPbf("comms.proto", "DrawResponse", proBuffer);
            let cardCipher = obj2.cardCipher;
            let st = decryptByECIES(cardCipher);
            salt = st;
            cards.push(st[CARD_INDEX]);
            errMsg = obj2.errMsg;
            console.log(obj2.errMsg);
            break;
        case 3:
            let obj3 = readPbf("comms.proto", "DrawNotification", proBuffer);
            let pk = obj3.pk;
            proof.push(obj3.proof);
            break;
        case 4:
            let obj4 = readPbf("comms.proto", "DrawLeftNotification", proBuffer);
            cards = cards.concat(obj4.cards);
            console.log(cards);
            salt = obj4.salt;
            console.log(obj4.errMsg);
            break;
        case 5:
            let obj5 = readPbf("comms.proto", "ReturnResponse", proBuffer);
            let numReturned = obj5.numReturned;
            console.log(numReturned);
            salt = obj5.salt;
            console.log(obj5.errMsg);
            break;
        case 6:
            let obj6 = readPbf("comms.proto", "ReturnNotification", proBuffer);
            pk = obj6.pk;
            let numReturned2 = obj6.numReturned;
            console.log(numReturned2);
            break;
    }
};

ws.onerror = function (evt) {
    onError(evt)
};

ws.onclose = function (evt) {

    console.log("Connection closed.");
};


//open游戏
async function openGame(deckNo, cards, decks, players, rounds) {

    let payload = {deckNo: deckNo, Cards: cards, Decks: decks, Players: players, rounds: rounds};

    let buffer = await createPbf("comms.proto", "OpenRequest", payload);

    ws.send(addOneByte(0, buffer));
}

//加入游戏
async function joinGame() {

    await createKey();

    let payload = {deckId: deckId, pk: new Uint8Array(pkBuffer)};

    let buffer = await createPbf("comms.proto", "JoinRequest", payload);

    ws.send(addOneByte(1, buffer));
}

//抓牌
async function drawCard() {

    while (errMsg == null) {
        let sign = await signByECDSA(salt);

        let payload = {deckId: deckId, pk: new Uint8Array(pkBuffer), sig: new Uint8Array(sign)};

        let buffer = await createPbf("comms.proto", "DrawRequest", payload);

        ws.send(addOneByte(2, buffer));
    }
    errMsg = null;
}

//抓剩余牌
async function drawLeftCards() {

    let sign = await signByECDSA(salt);

    let payload = {deckId: deckId, pk: new Uint8Array(pkBuffer), sig: new Uint8Array(sign)};

    let buffer = await createPbf("comms.proto", "DrawLeftRequest", payload);

    ws.send(addOneByte(3, buffer));
}

//还牌
async function returnCards(reSalt) {

    let sign = await signByECDSA(salt);

    let obj = encryptByECIES(dpk, reSalt);

    let payload = {
        deckId: deckId, pk: new Uint8Array(pkBuffer), sig: new Uint8Array(sign),
        cardsCipher: obj
    };

    let buffer = await createPbf("comms.proto", "ReturnRequest", payload);

    ws.send(addOneByte(4, buffer));
}


function onError(evt) {
    writeToScreen('<span style="color: red;">ERROR:</span> ' + evt.data);
}