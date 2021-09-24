//
//  RegexLexer.swift
//  SavannaKit
//
//  Created by Louis D'hauwe on 05/07/2018.
//  Copyright © 2018 Silver Fox. All rights reserved.
//

import Foundation

public typealias TokenTransformer = (_ range: Range<String.Index>) -> Token

extension RegexLexer {
	
	public func generateRegexTokens(_ generator: RegexTokenGenerator, source: String) -> [Token] {

		var tokens = [Token]()

		let fullNSRange = NSRange(location: 0, length: source.utf16.count)
		for numberMatch in generator.regularExpression.matches(in: source, options: [], range: fullNSRange) {
			
			guard let swiftRange = Range(numberMatch.range, in: source) else {
				continue
			}
			
			let token = generator.tokenTransformer(swiftRange)
			tokens.append(token)
			
		}
		
		return tokens
	}

}
