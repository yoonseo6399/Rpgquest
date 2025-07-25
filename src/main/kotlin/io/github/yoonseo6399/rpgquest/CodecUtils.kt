package io.github.yoonseo6399.rpgquest

import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

/**
 * kotlinx.serialization의 KSerializer를 Minecraft의 Codec으로 변환합니다.
 *
 * 이 Codec은 KSerializer를 사용하여 T 타입의 객체를 JSON 문자열로 직렬화하고,
 * 그 문자열을 Codec.STRING를 통해 저장합니다. 역직렬화는 그 반대 과정을 거칩니다.
 *
 * @param T 변환할 객체의 타입
 * @param serializer T 타입에 대한 KSerializer
 * @return T 타입을 위한 Codec
 */
fun <T : Any> kserializerToCodec(serializer: KSerializer<T>): Codec<T> {
    return Codec.STRING.comapFlatMap(
        { jsonString ->
            try {
                val obj = Json.decodeFromString(serializer, jsonString)
                DataResult.success(obj)
            } catch (e: Exception) {
                DataResult.error { "KSerializer decoding failed: ${e.message}" }
            }
        },
        { obj ->
            try {
                Json.encodeToString(serializer, obj)
            } catch (e: Exception) {
                throw IllegalStateException("KSerializer encoding failed: ${e.message}", e)
            }
        }
    )
}
