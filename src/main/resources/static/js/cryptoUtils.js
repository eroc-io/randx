const st = window.crypto.subtle;

if (!window.crypto || !st) {
    alert("This browser does not support the Web Cryptography API");
}

var pk = null;
var sk = null;
var keyPair = null;
var pkBuffer = null;


//生成获取ecdh密钥对
async function createKey() {

    let key = await  st.generateKey(
        {
            name: "ECDH",
            namedCurve: "P-256",
        },
        //whether the key is extractable
        true,
        //can be any combination of "deriveKey" and "deriveBits"
        ["deriveBits"]
    );

    keyPair = key;
    pk = key.publicKey;
    sk = key.privateKey;

    pkBuffer = await st.exportKey(
        'raw',
        pk
    );
}

//ECDH返回秘密
async function getSecret(othersPk) {

    let temPk = await st.importKey(
        'raw',
        othersPk,
        {
            name: 'ECDH',
            namedCurve: 'P-256'
        },
        false,
        []
    );

    let bits = await st.deriveBits(
        {
            name: 'ECDH',
            namedCurve: 'P-256',
            public: temPk
        },
        sk,
        256);
    return bits;
}


async function ecdhToEcdsa(keypair, extractable) {
    let pk = keypair.publicKey, sk = keypair.privateKey;
    console.assert(pk !== null && sk !== null, 'Not a valid key pair');
    console.assert(pk.algorithm !== null && pk.algorithm.name === 'ECDH' && sk.algorithm !== null && sk.algorithm.name === 'ECDH', 'Not a ECDH key pair');
    console.assert(pk.extractable && sk.extractable, 'Key pair is not extractable');
    console.assert(crypto !== null && crypto.subtle !== null, 'Web crypto is not available');

    extractable = extractable || false;
    let publicKey = await st.importKey('raw', await st.exportKey('raw', pk), {
            name: 'ECDSA',
            namedCurve: pk.algorithm.namedCurve
        },
        extractable,
        ['verify']
    );
    let privateKey = await st.importKey('jwk', await Object.assign(await st.exportKey('jwk', sk), {key_ops: ['sign']}), {
            name: 'ECDSA',
            namedCurve: sk.algorithm.namedCurve
        },
        extractable,
        ['sign']
    );
    return {privateKey, publicKey};
}

async function ecdsaToEcdh(keypair, extractable) {
    let pk = keypair.publicKey, sk = keypair.privateKey;
    console.assert(pk !== null && sk !== null, 'Not a valid key pair');
    console.assert(pk.algorithm !== null && pk.algorithm.name === 'ECDSA' && sk.algorithm !== null && sk.algorithm.name === 'ECDSA', 'Not a ECDSA key pair');
    console.assert(pk.extractable && sk.extractable, 'Key pair is not extractable');
    console.assert(crypto !== null && crypto.subtle !== null, 'Web crypto is not available');
    extractable = extractable || false;
    let publicKey = await st.importKey('raw', await st.exportKey('raw', pk), {
            name: 'ECDH',
            namedCurve: pk.algorithm.namedCurve
        },
        extractable,
        []
    );
    let privateKey = await st.importKey('jwk', await Object.assign(await st.exportKey('jwk', sk), {key_ops: ['deriveBits', 'deriveKey']}), {
            name: 'ECDH',
            namedCurve: sk.algorithm.namedCurve
        },
        extractable,
        ['deriveBits', 'deriveKey']
    );
    return {privateKey, publicKey};
}


//sha256加密
async function sha256(dataBuffer) {

    return await st.digest("SHA-256", dataBuffer);

}

//ECDSA签名
async function signByECDSA(dataBuffer) {

    //转换密钥对格式
    let key = await ecdhToEcdsa(keyPair, false);
    let skOfECDSA = key.privateKey;
    let signature = await st.sign(
        {
            name: "ECDSA",
            hash: {name: "SHA-256"},
        },
        skOfECDSA,
        dataBuffer
    );
    return signature;
}


//ECDSA验证
async function verifyByECDSA(otherPk, signature, dataBuffer) {
    let temPk = await st.importKey(
        'raw',
        otherPk,
        {
            name: 'ECDSA',
            namedCurve: 'P-256'
        },
        false,
        ['verify']);

    let bool = await st.verify(
        {
            name: "ECDSA",
            hash: {name: "SHA-256"},
        },
        temPk,
        signature,
        dataBuffer
    );

    return bool;
}


//ECIES加密
async function encryptByECIES(otherPk, dataBuffer) {

    let secrtHash = await sha256(await getSecret(otherPk));
    let encKey = secrtHash.slice(0, 16);
    let temEncKey = await st.importKey(
        'raw',
        encKey,
        {
            name: 'AES-CBC',
        },
        false,
        ['encrypt', 'decrypt']
    );

    let macKey = secrtHash.slice(16);

    let iv = await window.crypto.getRandomValues(new Uint8Array(16));

    let cipher = await st.encrypt(
        {
            name: 'AES-CBC',
            iv: iv,
        },
        temEncKey,
        dataBuffer
    );

    let dataToMac = concatUint8Array([iv, new Uint8Array(pkBuffer), new Uint8Array(cipher)]);


    let temMacKey = await st.importKey(
        'raw',
        macKey,
        {
            name: 'HMAC',
            hash: {name: 'SHA-256'},

        },
        false,
        ["sign", "verify"]
    );

    let mac = await st.sign(
        {
            name: 'HMAC',
            hash: {name: 'SHA-256'},
        },
        temMacKey,
        dataToMac
    );

    //Uint8Array()
    return {
        iv: iv, epk: new Uint8Array(pkBuffer), cipher: new Uint8Array(cipher), mac: new Uint8Array(mac)
    };
}


//ECIES解密
//body{iv,epk,cipher,mac},内部格式为Uint8Array()
async function decryptByECIES(bodyUint8Array) {

    let secrtHash = await sha256(await getSecret(bodyUint8Array.epk));
    let encKey = secrtHash.slice(0, 16);
    let macKey = secrtHash.slice(16);

    let temMacKey = await st.importKey(
        'raw',
        macKey,
        {
            name: 'HMAC',
            hash: {name: 'SHA-256'},

        },
        false,
        ["sign", "verify"]
    )
    let dataToMac = concatUint8Array([bodyUint8Array.iv, bodyUint8Array.epk, bodyUint8Array.cipher]);

    let macFlog = await st.verify(
        {
            name: 'HMAC',
            hash: {name: 'SHA-256'},
        }, temMacKey,
        bodyUint8Array.mac,
        dataToMac
    );

    if (!macFlog)
        throw new Error('Corrupted body - unmatched authentication code');

    let temEncKey = await st.importKey(
        'raw',
        encKey,
        {
            name: 'AES-CBC',
        },
        false,
        ['encrypt', 'decrypt']
    );

    let cipher = await st.decrypt(
        {
            name: 'AES-CBC',
            iv: bodyUint8Array.iv,
        },
        temEncKey,
        bodyUint8Array.cipher
    );

    return cipher;

}
