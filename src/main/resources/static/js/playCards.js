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
//座位号信息
var number = null;
//空桌信息
var emptySeat = null;
//Uint8Array，盐信息
var salt = null;
//[] Uint8Array，所持牌的所有盐信息
var salts = [];
//Uint8Array，服务器公钥
var dpk = null;
//Uint8Array，牌证明
var proofs = {};
//Uint8Array
var cards = [];
//其他玩家的公钥
var pks = [];
//有次序的其他玩家的公钥
var orderPks = {};

var ws = new WebSocket("ws://192.168.10.153:8080/ws");
ws.binaryType = "arraybuffer";

ws.onopen = function (evt) {

    console.log("Connection open ...");

};

ws.onmessage = async function getMessage(evt) {

    let buffer = evt.data;
    let [num, proBuffer] = subByte(buffer);

    switch (num) {
        case 0:
            //开局返回的deckId，responseId = 0
            let obj = await readPbf("comms.proto", "OpenResponse", proBuffer);
            deckId = obj.deckId;
            console.log('deckId:-----');
            console.log(deckId);
            console.log(obj.errMsg);
            break;
        case 1:
            //加入牌局返回的salt和服务器pk，responseId = 1
            let obj1 = await readPbf("comms.proto", "StartResponse", proBuffer);
            salt = obj1.salt;
            dpk = obj1.dpk;
            console.log('salt------' + salt);
            console.log('服务器pk-----' + dpk);
            console.log(obj1.errMsg);
            break;
        case 2:
            //抓牌返回的salt，取得牌信息储存，responseId = 2
            let obj2 = await readPbf("comms.proto", "DrawResponse", proBuffer);
            console.log(obj2.errMsg);
            if (!obj2.errMsg) {
                let cardCipher = obj2.cardCipher;
                salt = new Uint8Array(await decryptByECIES(cardCipher));
                salts.push(salt);
                cards.push(salt[CARD_INDEX]);
                console.log('手牌-----' + cards);
                await drawCard();
            }

            break;
        case 3:
            //其他玩家抓到牌的proof， responseId = 3
            let obj3 = await readPbf("comms.proto", "DrawNotification", proBuffer);
            let pk = obj3.pk;
            let pkBase64 = window.btoa(Uint8ArrayToString(pk));

            if (pks.indexOf(pkBase64) == -1) {
                pks.push(pkBase64);
                proofs[pkBase64] = [];
            }
            proofs[pkBase64].push(obj3.proof);
            // window.atob(pkBase64);

            break;
        case 4:
            //查看底牌返回的牌信息，取得牌信息储存，responseId = 4
            let obj4 = await readPbf("comms.proto", "DrawLeftNotification", proBuffer);
            console.log('底牌-----' + obj4.cards);
            salt = obj4.salt;
            console.log(obj4.errMsg);
            break;
        case 5:
            //还牌信息, responseId = 5
            let obj5 = await readPbf("comms.proto", "ReturnResponse", proBuffer);
            let numReturned = obj5.numReturned;
            console.log('还了 ' + numReturned + ' 张牌');
            salt = obj5.salt;
            console.log(obj5.errMsg);
            break;
        case 6:
            //其他玩家还牌信息, responseId = 6
            let obj6 = await readPbf("comms.proto", "ReturnNotification", proBuffer);

            console.log(window.btoa(Uint8ArrayToString(obj6.pk)) + '还了 ' + obj6.numReturned + ' 张牌');

            break;
        case 7:
            //加入牌局返回的自己编号和空桌信息，responseId = 7
            let obj7 = await readPbf("comms.proto", "JoinResponse", proBuffer);
            number = obj7.number;
            emptySeat = obj7.emptySeat;
            let joinNotifys = obj7.joinNotify;

            if (joinNotifys) {
                for (let join of joinNotifys) {
                    orderPks[join.joinSeat] = join.joinpk;
                }
            }
            orderPks[number] = new Uint8Array(pkBuffer);

            console.log(orderPks);

            console.log('分配的桌号------：' + number);
            if (emptySeat) {
                console.log('还缺' + emptySeat + '个人可开始游戏');
            } else {
                console.log('可以开始游戏了')
            }

            break;
        case 8:
            //其他玩家加入牌局返回的空桌信息，其他玩家编号和其他玩家pk，responseId = 8
            let obj8 = await readPbf("comms.proto", "JoinNotification", proBuffer);
            emptySeat = obj8.emptySeat;
            orderPks[obj8.joinSeat] = obj8.joinpk;

            console.log(obj8.joinSeat + '号桌加入了');
            if (emptySeat) {
                console.log('还缺' + emptySeat + '个人可开始游戏');
            } else {
                console.log('可以开始游戏了')
            }

            console.log(orderPks);
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

    let payload = {deckNo: deckNo, numCards: cards, numDecks: decks, numPlayers: players, rounds: rounds};

    let buffer = await createPbf("comms.proto", "OpenRequest", payload);

    ws.send(addOneByte(0, buffer));
};

//加入游戏 funcId = 1
async function joinGame() {

    await createKey();

    let payload = {deckId: deckId, pk: new Uint8Array(pkBuffer)};

    let buffer = await createPbf("comms.proto", "JoinRequest", payload);

    ws.send(addOneByte(1, buffer));
};


//抓牌 funcId = 2
async function drawCard() {

    let sign = await signByECDSA(salt);

    let payload = {deckId: deckId, pk: new Uint8Array(pkBuffer), sig: new Uint8Array(sign)};

    let buffer = await createPbf("comms.proto", "DrawRequest", payload);

    ws.send(addOneByte(2, buffer));

};

//剩余牌 funcId = 3
async function drawLeftCards() {

    let allPk = [];

    for (var i in orderPks) {
        //合并所有人的pk
        allPk.push(orderPks[i]);
    }

    let sign = await signByECDSA(await sha256(concatUint8Array(allPk)));

    let payload = {deckId: deckId, pk: new Uint8Array(pkBuffer), sig: new Uint8Array(sign)};

    let buffer = await createPbf("comms.proto", "DrawLeftRequest", payload);

    ws.send(addOneByte(3, buffer));
};


//还牌 funcId = 4
async function returnCards(reSalts) {

    let sign = await signByECDSA(salt);

    let macObj = await encryptByECIES(dpk, reSalts);

    let payload = {
        deckId: deckId, pk: new Uint8Array(pkBuffer), sig: new Uint8Array(sign),
        cardsCipher: macObj
    };

    let buffer = await createPbf("comms.proto", "ReturnRequest", payload);

    ws.send(addOneByte(4, buffer));
};


function onError(evt) {
    writeToScreen('<span style="color: red;">ERROR:</span> ' + evt.data);
};