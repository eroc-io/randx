async function createPbf(protoName, messageName, payload) {

    let root = await protobuf.load(protoName);

    let AwesomeMessage = root.lookupType(messageName);

    // Verify the payload if necessary
    let errMsg = AwesomeMessage.verify(payload);
    if (errMsg)
        throw Error(errMsg);

    // Create a new message
    let message = AwesomeMessage.create(payload);

    // Encode a message to an Uint8Array (browser) or Buffer (node)
    let buffer = AwesomeMessage.encode(message).finish();

    return buffer;

}

async function readPbf(protoName, messageName, buf) {

    let root = await protobuf.load(protoName);

    let AwesomeMessage = root.lookupType(messageName);

    let message = AwesomeMessage.decode(buf);

    // Maybe convert the message back to a plain object
    let object = AwesomeMessage.toObject(message, {
        longs: String,
        enums: String,
        bytes: ArrayBuffer,
    });

    return object;
}

//在arrayBuffer前面加一个字节数
function addOneByte(num, buffers) {

    let intBuffer = new Int8Array(buffers);
    let lenght = intBuffer.length + 1;
    let int8 = new Int8Array(lenght);

    int8[0] = num;

    int8.set(intBuffer, 1);

    return int8.buffer;
}

//去掉arrayBuffer前面的一个字节数
function subByte(buffer) {

    let intBuffer = new Int8Array(buffer);
    let one = intBuffer[0];
    let newBuffer = intBuffer.slice(1);
    return [one, newBuffer.buffer];
}


