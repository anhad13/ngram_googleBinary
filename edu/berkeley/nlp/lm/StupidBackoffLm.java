package edu.berkeley.nlp.lm;

import java.io.Serializable;
import java.util.List;

import edu.berkeley.nlp.lm.map.NgramMap;
import edu.berkeley.nlp.lm.util.LongRef;
import edu.berkeley.nlp.lm.values.CountValueContainer;

/**
 * Language model implementation which uses stupid backoff (Brants et al., 2007)
 * computation. Note that stupid backoff does not properly normalize, so the
 * scores this LM computes are not in fact probabilities.
 * 
 * @author adampauls
 * 
 * @param <W>
 */
public class StupidBackoffLm<W> extends AbstractArrayEncodedNgramLanguageModel<W> implements ArrayEncodedNgramLanguageModel<W>, Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected final NgramMap<LongRef> map;

	private final float alpha;

	public StupidBackoffLm(final int lmOrder, final WordIndexer<W> wordIndexer, final NgramMap<LongRef> map, final ConfigOptions opts) {
		super(lmOrder, wordIndexer, (float) opts.unknownWordLogProb);
		this.map = map;
		this.alpha = (float) opts.stupidBackoffAlpha;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.berkeley.nlp.lm.AbstractArrayEncodedNgramLanguageModel#getLogProb
	 * (int[], int, int)
	 */

        public float getLogProbK(final int[] ngram, final int startPos, final int endPos) {
                float total = 0.0f;
                int order = this.getLmOrder();
                for(int i=startPos; i<=endPos-order; i++)
                {
                  //if (i+5 > endPos)
                  // break;
                  float logp = getLogProb(ngram, i,i+order);
                  //System.out.println(i);
                  if(!Float.isNaN(logp))
                  	total = total + logp;
                }
                if(total == 0.0 || Float.isNaN(total))
		{
		  total = getLogProb(ngram, startPos, endPos);
		}
                return total;
        }
        @Override
	public float getLogProb(final int[] ngram, final int startPos, final int endPos) {
		final NgramMap<LongRef> localMap = map;
		float logProb = oovWordLogProb;
		long probContext = 0L;
		int probContextOrder = -1;
		long backoffContext = 0L;
		int backoffContextOrder = -1;

		final LongRef scratch = new LongRef(-1L);
                //System.out.println(ngram);
                //System.out.println(startPos);
                //System.out.println(endPos);
		for (int i = endPos - 1; i >= startPos; --i) {
			assert (probContext >= 0);
			probContext = localMap.getValueAndOffset(probContext, probContextOrder, ngram[i], scratch);
                        //System.out.println(probContext);
			if (probContext < 0) {
                                //System.out.println(i);
				return logProb;
			} else {
				final long currCount = scratch.value;
				long backoffCount = -1L;
				if (i == endPos - 1) {
					backoffCount = ((CountValueContainer) map.getValues()).getUnigramSum();
				} else {
					backoffContext = localMap.getValueAndOffset(backoffContext, backoffContextOrder++, ngram[i], scratch);
					backoffCount = scratch.value;
				}
				logProb = (float) Math.log(currCount / ((float) backoffCount) * pow(alpha, i - startPos));
				probContextOrder++;
			}

		}
		return logProb;
	}

	/**
	 * Gets the raw count of an n-gram.
	 * 
	 * @param ngram
	 * @param startPos
	 * @param endPos
	 * @return count of n-gram, or -1 if n-gram is not in the map.
	 */
	public long getRawCount(final int[] ngram, final int startPos, final int endPos) {
		final NgramMap<LongRef> localMap = map;
		long probContext = 0L;

		final LongRef scratch = new LongRef(-1L);
		for (int probContextOrder = -1; probContextOrder < endPos - startPos - 1; ++probContextOrder) {
			assert (probContext >= 0);
			probContext = localMap.getValueAndOffset(probContext, probContextOrder, ngram[endPos - probContextOrder - 2], scratch);
			if (probContext < 0) { return -1; }
		}
		return scratch.value;
	}

	private static float pow(final float alpha, final int n) {
		float ret = 1.0f;
		for (int i = 0; i < n; ++i)
			ret *= alpha;
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.berkeley.nlp.lm.AbstractArrayEncodedNgramLanguageModel#getLogProb
	 * (int[])
	 */
	@Override
	public float getLogProb(final int[] ngram) {
		return ArrayEncodedNgramLanguageModel.DefaultImplementations.getLogProb(ngram, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.berkeley.nlp.lm.AbstractArrayEncodedNgramLanguageModel#getLogProb
	 * (java.util.List)
	 */
	@Override
	public float getLogProb(final List<W> ngram) {
		return ArrayEncodedNgramLanguageModel.DefaultImplementations.getLogProb(ngram, this);
	}

	public NgramMap<LongRef> getNgramMap() {
		return map;
	}

}
