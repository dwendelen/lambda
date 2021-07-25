package se.daan.lambda

import se.daan.lambda.runner.Runner

fun main() {
    val thing = """
        Bool : A -> A -> A
        true : Bool = t -> f ->
            t
        false : Bool = t -> f ->
            f
        if : Bool -> A -> A -> A = cond -> t -> f ->
            cond.t.f
        not : Bool -> Bool = b ->
            b.false.true
        and : Bool -> Bool -> Bool = a -> b ->
            a.b.a
        or : Bool -> Bool -> Bool = a -> b ->
            a.a.b
        xor : Bool -> Bool -> Bool = a -> b ->
            a.(not.b).b
        eqBool : Bool -> Bool -> Bool = a -> b ->
            a.b.(not.b)
        printBool : Bool -> IO = a ->
            a.(printTrue).(printFalse)

        person : N -> A -> (N -> A -> R) -> R = name -> age ->
            fn ->
            fn.name.age
        name : N -> _ -> N = name -> age ->
            name
        age : _ -> A -> A = name -> age ->
            age
        
        List.I : (I -> Self.I -> R) -> R -> R
        list : I -> List.I -> List.I = item -> tail -> 
            onItem -> onNil ->
                onItem.item.tail
        nil : List.I = onItem -> onNil ->
            onNil
            
        map1 : Self -> (I -> O) -> List.I -> List.O = self ->
            fn -> node -> 
              node.(
                i -> t -> 
                    list.(fn.i).(self.self.fn.t)
              ).(
                nil
              )
        map: (I -> O) -> List.I -> List.O = map1.map1

        fold1 : Self -> (A -> I -> A) -> A -> List.I -> A = self ->
            fn -> acc -> node ->
                node.(
                    item -> tail ->
                        self.self.fn.(fn.acc.item).tail
                ).(
                    acc
                )
        fold : (A -> I -> A) -> A -> List.I -> A = fold1.fold1
        
        foldr1 : Self -> (I -> A -> A) -> A -> List.I -> A = self ->
            fn -> acc -> node ->
                node.(
                    item -> tail ->
                        fn.item.(self.self.fn.acc.tail)
                ).(
                    acc
                )
        foldr : (I -> A -> A) -> A -> List.I -> A = foldr1.foldr1
        
        printList : (A -> IO -> IO) -> List.A -> IO -> IO = printElement -> node -> cnt ->
            printLBracket.(
                foldr.(e -> c -> printElement.e.(printComma.c)).(printRBracket.cnt).node
            )
            
        eqList1 : Self -> (A -> A -> Bool) -> List.A -> List.A -> Bool = self
            -> eqElement -> node1 -> node2 ->
                node1.(e1 -> t1 ->
                    node2.(e2 -> t2 ->
                        if.(eqElement.e1.e2).(self.self.eqElement.t1.t2).false
                    ).(
                        false
                    )
                ).(
                    node2.(
                        false
                    ).(
                        true
                    )
                )
                
        eqList : (A -> A -> Bool) -> List.A -> List.A -> Bool = eqList1.eqList1
        
        all : List.Bool -> Bool = fold.and.true
        
        Bit: R -> R -> R
        0 : Bit = on0 -> on1 -> on0
        1 : Bit = on0 -> on1 -> on1
        BitAndCarry : (Bit -> Bit -> R) -> R
        adderBit : Bit -> Bit -> Bit -> BitAndCarry = a -> b -> c ->
            a.(
                b.(
                    c.(
                        fn -> fn.0.0
                    ).(
                        fn -> fn.1.0
                    )
                ).(
                    c.(
                        fn -> fn.1.0
                    ).(
                        fn -> fn.0.1
                    )
                )
            ).(
                b.(
                    c.(
                        fn -> fn.1.0
                    ).(
                        fn -> fn.0.1
                    )
                ).(
                    c.(
                        fn -> fn.0.1
                    ).(
                        fn -> fn.1.1
                    )
                )
            )
        plusBit : Bit -> Bit -> Bit = a -> b ->
            adderBit.a.b.0.(r -> c -> r)
        invertBit : Bit -> Bit = b ->
            b.1.0
        printBit : Bit -> IO -> IO = a -> 
            a.(print0).(print1)
        eqBit : Bit -> Bit -> Bool =
            a -> b -> 
                a.(
                    b.true.false
                ).(
                    b.false.true
                )
        
        Hex: (Bit -> Bit -> Bit -> Bit -> R) -> R
        hex : Bit -> Bit -> Bit -> Bit -> Hex =
            b3 -> b2 -> b1 -> b0 -> fn -> fn.b3.b2.b1.b0
        hex0 : Hex = hex.0.0.0.0
        hex1 : Hex = hex.0.0.0.1
        hex2 : Hex = hex.0.0.1.0
        hex3 : Hex = hex.0.0.1.1
        hex4 : Hex = hex.0.1.0.0
        hex5 : Hex = hex.0.1.0.1
        hex6 : Hex = hex.0.1.1.0
        hex7 : Hex = hex.0.1.1.1
        hex8 : Hex = hex.1.0.0.0
        hex9 : Hex = hex.1.0.0.1
        hexa : Hex = hex.1.0.1.0
        hexb : Hex = hex.1.0.1.1
        hexc : Hex = hex.1.1.0.0
        hexd : Hex = hex.1.1.0.1
        hexe : Hex = hex.1.1.1.0
        hexf : Hex = hex.1.1.1.1
        
        HexAndCarry : (Hex -> Bit -> R) -> R
        adderHex: Hex -> Hex -> Bit -> HexAndCarry =
            h1 -> h2 -> c ->
                h1.(b13 -> b12 -> b11 -> b10 -> 
                    h2.(b23 -> b22 -> b21 -> b20 ->
                        adderBit.b10.b20.c.(b0 -> c0 ->
                            adderBit.b11.b21.c0.(b1 -> c1 ->
                                adderBit.b12.b22.c1.(b2 -> c2 ->
                                    adderBit.b13.b23.c2.(b3 -> c3 ->
                                        hc -> hc.(hex.b3.b2.b1.b0).c3
                                    )
                                )
                            )
                        )
                    )
                )
        printHex : Hex -> IO -> IO = 
            h -> h.(d3 -> d2 -> d1 -> d0 ->
                d3.(
                    d2.(
                        d1.(
                            d0.print0.print1
                        ).(
                            d0.print2.print3
                        )
                    ).(
                        d1.(
                            d0.print4.print5
                        ).(
                            d0.print6.print7
                        )
                    )
                ).(
                    d2.(
                        d1.(
                            d0.print8.print9
                        ).(
                            d0.printa.printb
                        )
                    ).(
                        d1.(
                            d0.printc.printd
                        ).(
                            d0.printe.printf
                        )
                    )
                )
            )
        eqHex: Hex -> Hex -> Bool = h1 -> h2 ->
            h1.(b13 -> b12 -> b11 -> b10 ->
                h2.(b23 -> b22 -> b21 -> b20 ->
                    and.(
                        and.(eqBit.b13.b23).(eqBit.b12.b22)
                    ).(
                        and.(eqBit.b11.b21).(eqBit.b10.b20)
                    )
                )
            )
        invertHex : Hex -> Hex = h ->
            h.(b3 -> b2 -> b1 -> b0 ->
                hex.(invertBit.b3).(invertBit.b2).(invertBit.b1).(invertBit.b0)
            )
            
        Byte : (Hex -> Hex -> R) -> R
        byte : Hex -> Hex -> Byte = h1 -> h0 ->
            fn -> fn.h1.h0
        byte0 : Byte = byte.hex0.hex0
        byte1 : Byte = byte.hex0.hex1
        ByteAndCarry : (Byte -> Bit -> R) -> R
        adderByte : Byte -> Byte -> Bit -> ByteAndCarry = b1 -> b2 -> c ->
            b1.(h11 -> h10 ->
                b2.(h21 -> h20 ->
                    adderHex.h10.h20.c.(h0 -> c0 ->
                        adderHex.h11.h21.c0.(h1 -> c1 ->
                            bc -> bc.(byte.h1.h0).c1
                        )
                    )
                )
            )
        plusByte : Byte -> Byte -> Byte = b1 -> b2 ->
            adderByte.b1.b2.0.(b -> c -> b)
        eqByte : Byte -> Byte -> Bool = b1 -> b2 ->
            b1.(b11 -> b10 ->
                b2.(b21 -> b20 ->
                    and.(eqHex.b11.b21).(eqHex.b10.b20)
                )
            )
        printByte : Byte -> IO -> IO = b -> cnt ->
            b.(h1 -> h0 ->
                printHex.h1.(printHex.h0.cnt)
            )
        invertByte : Byte -> Byte = b ->
            b.(h1 -> h0 -> 
                byte.(invertHex.h1).(invertHex.h0)
            )
        negateByte : Byte -> Byte = b ->
            plusByte.(invertByte.b).byte1
        incrByte : Byte -> Byte =
            plusByte.byte1
        decrByte : Byte -> Byte =
            plusByte.(negateByte.byte1)
        
        infiniteList1 : Self -> List.Bool = self -> list.true.(self.self)
        infiniteList : List.Bool = infiniteList1.infiniteList1
        
        take1 : Self -> Byte -> List.I -> List.I = self -> 
            n -> node ->
                if.(eqByte.byte0.n).(
                    nil
                ).(
                    node.(head -> tail ->
                        list.head.(self.self.(decrByte.n).tail)
                    ).(
                        nil
                    )
                )
        take : Byte -> List.I -> List.I = take1.take1
        
        Loop : (Bool -> Loop -> R) -> R
        loop3Of3 : Loop -> Loop = first -> (fn -> fn.true.(first.first))
        loop2Of3 : Loop -> Loop = first -> (fn -> fn.false.(loop3Of3.first))
        loop1Of3 : Loop -> Loop = first -> (fn -> fn.false.(loop2Of3.first))
        loopOf3 : Loop = loop1Of3.loop1Of3
        
        loop5Of5 : Loop -> Loop = first -> (fn -> fn.true.(first.first))
        loop4Of5 : Loop -> Loop = first -> (fn -> fn.false.(loop5Of5.first))
        loop3Of5 : Loop -> Loop = first -> (fn -> fn.false.(loop4Of5.first))
        loop2Of5 : Loop -> Loop = first -> (fn -> fn.false.(loop3Of5.first))
        loop1Of5 : Loop -> Loop = first -> (fn -> fn.false.(loop2Of5.first))
        loopOf5 : Loop = loop1Of5.loop1Of5
        
        fizzBuzz1 : Self -> Byte -> Loop -> Loop -> IO -> IO = self ->
            num -> loop3 -> loop5 ->
                loop3.(is3 -> next3 -> 
                    loop5.(is5 -> next5 ->
                        if.is3.(
                            if.is5.(
                                printFizz.printSpace.printBuzz.printLn
                            ).(
                                printFizz.printLn
                            )
                        ).(
                            if.is5.(
                                printBuzz.printLn
                            ).(
                                printByte.num.printLn
                            )
                        ).(self.self.(incrByte.num).next3.next5)
                    )
                )
        fizzBuzz : IO -> IO =
            fizzBuzz1.fizzBuzz1.byte1.loopOf3.loopOf5
        
        prgm : IO = fizzBuzz.done
        
    """.trimIndent() + "\n"

    Runner()
        .io("printLn") { println() }
        .io("printSpace") { print(" ") }
        .io("printTrue") { print("true") }
        .io("printFalse") { print("false") }
        .io("print0") { print("0") }
        .io("print1") { print("1") }
        .io("print2") { print("2") }
        .io("print3") { print("3") }
        .io("print4") { print("4") }
        .io("print5") { print("5") }
        .io("print6") { print("6") }
        .io("print7") { print("7") }
        .io("print8") { print("8") }
        .io("print9") { print("9") }
        .io("printa") { print("a") }
        .io("printb") { print("b") }
        .io("printc") { print("c") }
        .io("printd") { print("d") }
        .io("printe") { print("e") }
        .io("printf") { print("f") }
        .io("printLBracket") { print("[") }
        .io("printRBracket") { print("]") }
        .io("printComma") { print(",") }
        .io("printFizz") { print("Fizz") }
        .io("printBuzz") { print("Buzz") }
        .run(thing, "prgm")
}